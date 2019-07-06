package com.sec.service.portal;

import com.google.common.collect.Maps;
import com.sec.async.EventModel;
import com.sec.async.EventType;
import com.sec.common.Codes;
import com.sec.common.Resp;
import com.sec.dao.ProductMapper;
import com.sec.pojo.Product;
import com.sec.pojo.User;
import com.sec.service.func.AsyncService;
import com.sec.service.func.RedisService;
import com.sec.util.ConvertUtil;
import com.sec.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillService {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private ProductMapper productMapper;


    /**
     * 暂时先简单加个可重入锁进行同步
     */
    private static final ReentrantLock lock = new ReentrantLock();


    /**
     * 标识无效的productId, 用于解决缓存穿透
     */
    private static final int INVALID_PRODUCT_ID = -1;

    /**
     * 缓存穿透无效Key的过期时间
     */
    private static final int INVALID_PRODUCT_EXPIRE = 30;

    private static final String MD5_HKEY_NAME = "md5";
    /**
     * 1. 用来暴露秒杀接口
     * 2. 把在秒杀中需要的数据在redis提前缓存好
     *
     * @param productId
     * @return
     */
    public Resp exposeUrl(Integer productId) {
        // 简单校验一下参数
        if (productId == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "商品id不能为空");
        }

        // todo 这里redis最好定时提前预热一下数据, 避开缓存击穿的风险
        // todo 预热同时也要避免缓存雪崩的风险, 可以设置不同的过期时间缓解一下
        // 先尝试查看Redis中是否已经存在了此hash健
        String productKey = RedisService.SECKILL_PRODUCT_PREFIX + String.valueOf(productId);
        boolean productExisted = redisService.hmexist(productKey);
        if (!productExisted) {
            // 没有就查数据库并放进redis里, 注意同步
            // todo 未来需要改成redis分布式锁来让多主机多进程之间也保证同步. 这里先简单同步一下
            lock.lock();
            try {
                Product product = productMapper.selectByPrimaryKey(productId);
                if (product != null) {
                    redisService.hmput(productKey, ConvertUtil.objectToMap(product));
                    redisService.expire(productKey, product.getEndTime());
                }
                // 数据库里没有, 可能发生缓存穿透, 需要注意!!
                // 处理办法: 仍然放入缓存, 但是需要设置一个比较短的有效时间, 这里我设置30秒
                else {
                    // 放入一个特殊的商品来标识, 这里我放一个主键是INVALID_PRODUCT_ID的商品代表是无效的productId
                    Product invalidProduct = new Product();
                    invalidProduct.setId(INVALID_PRODUCT_ID);
                    redisService.hmput(productKey, ConvertUtil.objectToMap(invalidProduct));
                    redisService.expire(productKey, INVALID_PRODUCT_EXPIRE);
                }
            } finally {
                lock.unlock();
            }
        }

        // 缓解缓存穿透的风险
        Integer id = redisService.hget(productKey, "id", Integer.class);
        if (id == INVALID_PRODUCT_ID) {
            return Resp.error(Codes.SERVER_ERR.getCode(), "无此商品");
        }

        // 判断秒杀是否开始
        Timestamp startTime = redisService.hget(productKey, "startTime", Timestamp.class);
        Timestamp endTime = redisService.hget(productKey, "endTime", Timestamp.class);
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        if (curTime.compareTo(startTime) < 0) {
            return Resp.error(Codes.SERVER_ERR.getCode(), "秒杀未开始");
        } else if (curTime.compareTo(endTime) > 0) {
            return Resp.error(Codes.SERVER_ERR.getCode(), "秒杀已结束");
        }

        // 秒杀开始就返回一个MD5串, 并存进redis
        // 先尝试获取MD5串
        boolean md5Existed = redisService.hexist(productKey, MD5_HKEY_NAME);
        if (!md5Existed) {
            // todo 未来需要改成redis分布式锁来让多主机多进程之间也保证同步. 这里先简单同步一下
            lock.lock();
            try {
                // 此时还是null说明还没人成功生成, 那么就生成一个MD5串, 并放入redis
                String md5 = MD5Util.getMD5(UUID.randomUUID().toString());
                redisService.hput(productKey, MD5_HKEY_NAME, md5);
            } finally {
                lock.unlock();
            }
        }

        return Resp.success(redisService.hget(productKey, MD5_HKEY_NAME, String.class));
    }


    /**
     * 1. 能带着正确的凭证调用这个接口, 说明productId对应的商品的秒杀已经开始了
     * 2. 真正的执行秒杀的业务
     *
     * @param certificate
     * @param productId
     * @return
     */
    public Resp executeSeckill(String certificate, Integer productId) {
        // 简单校验一下
        if (StringUtils.isBlank(certificate) || productId == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT);
        }

        // 检测凭证
        String productKey = RedisService.SECKILL_PRODUCT_PREFIX + String.valueOf(productId);
        String redisCertificate = redisService.hget(productKey, MD5_HKEY_NAME, String.class);
        // 凭证为空, 有两种情况:
        // 1: 秒杀未开始.
        // 2: 秒杀已开始, 但未有人调用过exposeUrl接口.（可以通过预热避免这种情况）
        // 解决条件二: 保证秒杀开始后, 缓存里一定有正确的凭证
        if (redisCertificate == null) {
            this.exposeUrl(productId);
            redisCertificate = redisService.hget(productKey, MD5_HKEY_NAME, String.class);
        }
        // 解决条件一/二: 保证过滤掉不正确的凭证
        if (!StringUtils.equals(certificate, redisCertificate)) {
            return Resp.error(Codes.SECKIIL_ERROR.getCode(), "不正确的凭证");
        }

        // 检测成功, 说明秒杀已开始
        // 获得当前的登录用户, 若此时未登录会返回需要登录的error信息, 并在advice包进行异常全局捕获.
        Integer userId = userService.getUserIdByToken();

        // 判断当前用户是否已经秒杀成功? 一人只允许秒杀一件
        String successKey = RedisService.SECKILL_SUCCESS_PREFIX + productId.toString();
        boolean hasSuccess = redisService.sismember(successKey, userId.toString());
        if(hasSuccess) {
            // return Resp.error(Codes.SECKIIL_ERROR.getCode(), "请不要重复下单");
        }

        // redis预减库存防止超卖
        // 预减库存之后若小于0直接返回秒杀失败
        Long stock = redisService.hdecr(productKey, "stock");
        if (stock < 0) {
            // 在加回去, 很有必要, 当订单生成失败/用户选择不支付的时候, 会incr stock
            redisService.hincr(productKey, "stock");
            return Resp.error(Codes.SECKIIL_ERROR.getCode(), "对不起, 商品已经被抢光啦");
        }

        // 预减成功, 异步生成订单, 放入任务队列, 并将成功下单信息userId + productId放入redis, 避免重复下单
        // todo 未来可考虑改为消息队列实现异步消费事件
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("productId", productId);
        EventModel model = new EventModel(EventType.SECKILL, params);
        asyncService.put(model);
        redisService.sadd(successKey, userId.toString());
        Timestamp endTime = redisService.hget(productKey, "endTime", Timestamp.class);
        redisService.expire(successKey, endTime);

        // 返回排队中, 客户端轮询订单是否生成决定支付
        return Resp.successMsg("正在排队下单中...");
    }
}
