package io.moyada.medivh.regulation;

/**
 * 长度校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class SizeRegulation extends BaseRegulation {

    final static byte STRING = 0;
    final static byte ARRAY = 1;
    final static byte COLLECTION = 2;

    private Integer min;
    private Integer max;

    private byte type;

    SizeRegulation(Integer min, Integer max, byte type) {
        this.min = min;
        this.max = max;
        this.type = type;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public boolean isStr() {
        return type == STRING;
    }

    public boolean isArr() {
        return type == ARRAY;
    }

    @Override
    public String toString() {
        return "SizeRegulation{" +
                "min=" + min +
                ", max=" + max +
                ", type=" + type +
                '}';
    }
}
