package cn.moyada.function.validator.core;

/**
 * 数字校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NumberValidation extends BaseValidation {

    private long min;

    private long max;

    public NumberValidation(long min, long max) {
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
