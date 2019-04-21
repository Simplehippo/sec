package com.sec.async.handler;

import com.sec.async.EventHandler;
import com.sec.async.EventModel;
import com.sec.async.EventType;
import com.sec.service.func.RedisService;
import com.sec.service.portal.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SeckillHandler implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(SeckillHandler.class);

    @Autowired
    private DataSourceTransactionManager tx;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    private AtomicInteger threadNo = new AtomicInteger(0);

    private ExecutorService executor = new ThreadPoolExecutor(
            200,
            500,
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "seckill handler thread - " + threadNo.getAndIncrement());
                    return thread;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()
    );

    @Override
    public void doHandle(EventModel model) {
        Map<String, Object> params = model.getParams();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Integer userId = (Integer)params.get("userId");
                Integer productId = (Integer)params.get("productId");
                // 在new出来的线程内部, 需要手动事务
                // 若不明白请自行百度Spring事务生效条件
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                definition.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
                definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionStatus status = tx.getTransaction(definition);
                try{
                    // 事务作业
                    // 注意这里userId是必要的, 因为在这个线程内部拿不到请求时那个线程的ThreadLocal变量
                    orderService.createOrder(userId, productId); // 生成订单, 不包含配送地址信息

                    // 成功创建订单, 提交事务
                    tx.commit(status);
                } catch (Exception e) {
                    // 创建订单失败, 回滚事务
                    tx.rollback(status);

                    // 做一些下单失败的清理和恢复工作
                    // 将用户成功下单信息删除, 将redis中的库存+1
                    redisService.del(RedisService.SECKILL_SUCCESS_PREFIX, userId.toString() + productId.toString());
                    redisService.incr(RedisService.SECKILL_STOCK_PREFIX, String.valueOf(productId));

                    // 打印失败日志
                    log.info("创建订单失败! userId: {}, productId: {}", userId, productId);
                } finally {

                }
            }
        };
        executor.execute(task);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.SECKILL);
    }
}

