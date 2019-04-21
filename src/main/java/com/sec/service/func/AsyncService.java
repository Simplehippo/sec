package com.sec.service.func;

import com.sec.async.EventLoop;
import com.sec.async.EventModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AsyncService {

    private static final Logger log = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
    private EventLoop eventLoop;

    private AtomicInteger processedCount = new AtomicInteger(0);

    public void put(EventModel model) {
        log.info("<!>--> AsyncService model-type: {}, orderNum: {}",
                model.getType().name(),
                processedCount.getAndIncrement());

        eventLoop.putEvent(model);
    }

}

