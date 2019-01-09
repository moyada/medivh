package cn.moyada.test.example;

import io.moyada.medivh.annotation.DecimalMax;
import io.moyada.medivh.annotation.DecimalMin;
import io.moyada.medivh.annotation.Max;
import io.moyada.medivh.annotation.Min;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class Counter {

    @Min(0)
    private int count;

    @DecimalMin(0.0)
    @DecimalMax(0.75)
    private float loadFactor;

    @DecimalMin(-20.5)
    @Max(100)
    private Double lastest;

    @Min(1)
    @Max(1)
    private byte type;
}
