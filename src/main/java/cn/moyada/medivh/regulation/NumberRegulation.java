package cn.moyada.medivh.regulation;

/**
 * 数字校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NumberRegulation extends BaseRegulation {

    private long min;

    private long max;

    NumberRegulation(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }
}
