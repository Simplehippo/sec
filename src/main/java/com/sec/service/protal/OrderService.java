package com.sec.service.protal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sec.common.Codes;
import com.sec.common.Const;
import com.sec.common.Resp;
import com.sec.dao.OrderMapper;
import com.sec.dao.PayInfoMapper;
import com.sec.dao.ProductMapper;
import com.sec.dao.ShippingMapper;
import com.sec.pojo.*;
import com.sec.service.func.RedisService;
import com.sec.util.FTPUtil;
import com.sec.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    private static AlipayTradeService tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().setCharset("utf-8").build();
    }

    public Resp createOrder(Integer userId, Integer productId) {
        log.info("create order -> {} : {}", userId, productId);

        // 从redis中获得商品信息, 注意这时候redis中的库存数据是与数据库不一致的, 我们保证最终一致即可
        Product product = redisService.get(RedisService.SECKILL_PRODUCT_PREFIX, productId.toString(), Product.class);
        BigDecimal payment = product.getPrice();

        // 生成订单号
        long currentTime = System.currentTimeMillis();
        Long orderNo = Long.valueOf(String.format("%d%d%d", currentTime, userId, new Random().nextInt(100)));
        Integer status = Const.OrderStatus.NO_PAY.getCode();

        // 生成一个新订单
        Order newOrder = new Order();
        newOrder.setOrderNo(orderNo);
        newOrder.setUserId(userId);
        newOrder.setProductId(productId);
        newOrder.setShippingId(null);
        newOrder.setPayment(payment);
        newOrder.setStatus(status);
        newOrder.setPaymentTime(null);
        newOrder.setSendTime(null);
        newOrder.setFinishTime(null);

        // 时间设置
        Timestamp curTime = new Timestamp(currentTime);
        // plus 30m
        Timestamp closeTime = Timestamp.from(
                LocalDateTime.ofInstant(curTime.toInstant(), ZoneId.systemDefault())
                .plusMinutes(30)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
        newOrder.setCloseTime(closeTime);
        newOrder.setCreateTime(curTime);
        newOrder.setUpdateTime(curTime);

        // 存入数据库
        orderMapper.insertSelective(newOrder);

        // 返回生成的订单号给前端数据
        return Resp.success(orderNo);
    }

    // todo 更新地址信息
    public Resp updateOrder(Long orderNo, Integer shippingId) {
        // 得到登录用户id
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        // 查出订单详情, 注意避免横向越权
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null) {
            return Resp.error(Codes.ORDER_ERROR.getCode(), "没有此订单");
        }

        // 查出地址信息
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId, shippingId);
        if(shipping == null) {
            return Resp.error(Codes.ORDER_ERROR.getCode(), "没有此地址");
        }

        // 更新订单
        order.setShippingId(shipping.getId());
        orderMapper.updateByPrimaryKeySelective(order);

        return Resp.success();
    }

    /**
     * 只在当前正在参与秒杀的时候使用
     * 用来轮询获取订单信息, 获取到了说明订单已经生成成功了
     * @param productId
     * @return
     */
    public Resp pollOrderDetail(Integer productId) {
        // 得到登录用户id
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        // 拿出缓存的商品
        Product product = redisService.get(RedisService.SECKILL_PRODUCT_PREFIX, productId.toString(), Product.class);
        Timestamp startTime = product.getStartTime();
        Timestamp endTime = product.getEndTime();

        // 考虑到同一个商品可能会参与多次秒杀活动
        // 查出订单详情需要保证:
        // 1. user_id = userId                                          // 避免横向越权
        // 2. product_id = productId                                    // 具体的商品id
        // 3. status = Const.OrderStatus.NO_PAY.getCode()               // 区分未支付订单和其他订单
        // 4. create_time > startTime                                   // 用来与过去的秒杀活动区分
        // 5. create_time < endTime                                     // 用来与过去的秒杀活动区分
        // 正常情况下同一个商品一个人只能秒杀一个, 去掉中途取消的订单干扰即可, 保证拿到的订单是唯一的
        // 考虑到检索效率利用上主键索引 order by id desc
        Order order = orderMapper.selectByUserIdProductIdAndTime(
                userId,
                productId,
                Const.OrderStatus.NO_PAY.getCode(),
                startTime,
                endTime
        );
        if(order == null) {
            return Resp.error(Codes.ORDER_ERROR.getCode(), "没有此订单");
        }

        return Resp.success(order);
    }

    public Resp getOrderDetail(Long orderNo) {
        // 得到登录用户id
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        // 查出订单详情, 注意避免横向越权
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null) {
            return Resp.error(Codes.ORDER_ERROR.getCode(), "没有此订单");
        }

        return Resp.success(order);
    }

    public Resp getOrderList(Integer pageNum, Integer pageSize) {
        // 得到登录用户id
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        // 查出订单信息即可, 注意带上分页
        Integer offset = (pageNum - 1) / pageSize;
        List<Order> orders = orderMapper.selectByUserIdAndLimit(userId, offset, pageSize);
        Integer total = orderMapper.countByUserId(userId);

        // 设置返回参数
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", total);
        map.put("orders", orders);
        return Resp.success(map);
    }

    public Resp cancel(Long orderNo) {
        // 得到登录用户id
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();

        // 查出订单详情, 注意避免横向越权
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null) {
            return Resp.error(Codes.ORDER_ERROR.getCode(), "没有此订单");
        }

        // 更新数据库订单状态为已取消
        order.setStatus(Const.OrderStatus.CANCELED.getCode());
        orderMapper.updateByPrimaryKeySelective(order);

        // 若更新成功, 执行redis更新操作
        // 将redis该商品id对应的数量+1
        Integer productId = order.getProductId();

        // 解决并发取消问题
        // 是基于取消了之后没有了成功信息
        // 主要是为了避免库存多次增加的问题
        // 涉及到redis库存操作的都需要慎重同步
        String hasSuccess = redisService.get(RedisService.SECKILL_SUCCESS_PREFIX, userId.toString() + productId.toString(), String.class);
        if(hasSuccess != null) {
            // todo 简单同步一下
            synchronized (this) {
                hasSuccess = redisService.get(RedisService.SECKILL_SUCCESS_PREFIX, userId.toString() + productId.toString(), String.class);
                if(hasSuccess != null) {
                    // 将redis该商品id对应的数量+1
                    redisService.incr(RedisService.SECKILL_STOCK_PREFIX, productId.toString());
                    // 将redis中此用户下单成功的缓存删除, 即可以让用户继续下单, 接着参与秒杀
                    redisService.del(RedisService.SECKILL_SUCCESS_PREFIX, userId.toString() + productId.toString());
                }
            }
        }

        return Resp.success();
    }

    public Resp pay(Long orderNo) {
        // 查出登录用户信息
        User loginUser = userService.getUserByToken();
        Integer userId = loginUser.getId();
        if(orderNo == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "参数错误!");
        }

        // 用一个Map保存一些支付参数
        Map<String, String> resultMap = Maps.newHashMap();

        // 查出订单信息
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if(order == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "用户没有该订单");
        }

        // 判断订单的状态
        if(Const.OrderStatus.NO_PAY.getCode() != order.getStatus()) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "订单状态异常");
        }

        // 检查是否设置了地址信息, 没有地址当然不能支付
        if(order.getShippingId() == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "请选择一个可用的送货地址");
        }

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        resultMap.put("orderNo", outTradeNo);

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("欢迎参与秒杀, 订单号: ").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为30分钟
        String timeoutExpress = "30m";

        // 商品明细列表，需填写购买商品详细信息
        List<GoodsDetail> goodsDetailList = new ArrayList<>();
        Product product = productMapper.selectByPrimaryKey(order.getProductId());
        GoodsDetail good = GoodsDetail.newInstance(
                product.getId().toString(),
                product.getName(),
                product.getPrice().multiply(new BigDecimal(100)).longValue(),  // 单位是分, 所以要乘100
                1
        );
        goodsDetailList.add(good);

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.call_back.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();

                // 获取暂存文件夹的位置, 没有就创建一个
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String path = request.getSession().getServletContext().getRealPath("upload");
                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 生成二维码, 注意path后一定要加/
                String qrPath = String.format(path + "/qr-%d-%s.png", userId, response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                log.info("qrPath:" + qrPath);

                // 准备上传到ftp服务器
                File targetFile = new File(qrPath);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("二维码上传至ftp服务器失败!", e);
                    e.printStackTrace();
                    return Resp.error(Codes.PAY_ERROR.getCode(), "二维码上传至ftp服务器失败!");
                }

                resultMap.put("qr_url", PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName());
                return Resp.success(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return Resp.error(Codes.PAY_ERROR.getCode(), "支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return Resp.error(Codes.PAY_ERROR.getCode(), "系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return Resp.error(Codes.PAY_ERROR.getCode(), "不支持的交易状态，交易返回异常!!!");
        }
    }

    @Transactional
    public Resp alipayCallback(HttpServletRequest request) {
        // 获取参数信息
        Map<String, String> params = Maps.newHashMap();
        Map requestParams = request.getParameterMap();

        for(Object keyObject : requestParams.keySet()) {
            String key = (String)keyObject;
            String[] values = (String[])requestParams.get(key);
            String valueStr = "";
            for(int i=0; i<values.length; i++) {
                String v = values[i];
                valueStr += (i == values.length - 1) ? v : v + ",";
            }
            params.put(key, valueStr);
        }

        log.info("支付宝回调,sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

        //验签,很重要,并且要防重复
        //移除sign和sign_type字段
//        params.remove("sign");   //原因:alipay sdk已经移除
        params.remove("sign_type");
        try {
            //一定要注意是alipay的公钥,不是用户公钥!!!!!!!!!!!!
            boolean alipayRSACheckedV2 =  AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if(!alipayRSACheckedV2) {
                return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "非法请求!");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调异常!", e);
            e.printStackTrace();
        }

        // 支付宝验签成功, 可以开始具体业务处理了
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);

        // 验证是否是本系统订单
        if(order == null) {
            return Resp.error(Codes.ORDER_ERROR.getCode(), "不是本系统的订单!");
        }

        // 验证是否重复回调,保证接口的幂等性
        if(order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return Resp.success("订单已支付, 不可再支付");
        }

        // 判断回调状态, 现在只考虑支付成功的回调状态, 支付中状态不太重要, 暂时不考虑
        if(StringUtils.equalsIgnoreCase(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS, tradeStatus)) {
            // 是支付成功的回调, 执行具体业务
            order.setStatus(Const.OrderStatus.PAID.getCode());
            order.setPaymentTime(new Timestamp(System.currentTimeMillis()));
            order.setFinishTime(new Timestamp(System.currentTimeMillis()));
            order.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            orderMapper.updateByPrimaryKeySelective(order);

            // 新增支付条例
            PayInfo payInfo = new PayInfo();
            payInfo.setUserId(order.getUserId());
            payInfo.setOrderNo(order.getOrderNo());
            payInfo.setPlatformNumber(tradeNo);
            payInfo.setPlatformStatus(tradeStatus);
            payInfo.setCreateTime(new Timestamp(System.currentTimeMillis()));
            payInfo.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            payInfoMapper.insertSelective(payInfo);

            // todo 生成成功,我们要减少相应商品的库存 order.getProductId()
            // 乐观锁/悲观锁都可以, 支付成功的并发量应该不会大
            // 因为商品不可能超卖, 同时只能有剩余库存数的订单在等待支付
            // 只有两种情况会发生redis库存增加:
            // 1. 订单生成失败
            // 2. 用户取消支付
            // ?后期可以加一个30m超时?
            // 当然支付这里不必考虑这些情况
            // 这里我使用乐观锁的机制
            productMapper.decrementStockById(order.getProductId());
        }

        return Resp.success();
    }

}
