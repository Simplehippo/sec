package com.sec;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TxTest {

    @Autowired
    private T2Service t2Service;

    @Test
    public void test() {
        t2Service.t2();
    }
}
