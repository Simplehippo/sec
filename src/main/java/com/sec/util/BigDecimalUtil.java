package com.sec.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    private BigDecimalUtil(){

    }

    public static BigDecimal add(BigDecimal b1,BigDecimal b2){
        return b1.add(b2);
    }

    public static BigDecimal sub(BigDecimal b1,BigDecimal b2){
        return b1.subtract(b2);
    }


    public static BigDecimal mul(BigDecimal b1,BigDecimal b2){
        return b1.multiply(b2);
    }

    public static BigDecimal div(BigDecimal b1,BigDecimal b2){
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数

        //除不尽的情况
    }

}
