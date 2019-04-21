package com.sec;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class T1Service {

    @Transactional
    public void t1() {
//        try {
            int i = 1 / 0;
//        } catch (Exception e) {
//            System.out.println("t1");
//            throw new Exception("我是t1跑出的异常");
//        }
    }
}
