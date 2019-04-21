package com.sec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class T2Service {

    @Autowired
    private DataSourceTransactionManager tx;

    @Autowired
    private T1Service t1Service;

    public void t2() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = tx.getTransaction(definition);
        try {
            t1Service.t1();
            tx.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            tx.rollback(status);
        }
    }
}
