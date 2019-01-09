package cn.moyada.test.example;

import io.moyada.medivh.annotation.NumberRule;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class Counter {

    @NumberRule(min = "0")
    private int count;

    @NumberRule(min = "-20.5", max = "100")
    private Double lastest;

    @NumberRule(min = "1", max = "1")
    private byte type;
}
