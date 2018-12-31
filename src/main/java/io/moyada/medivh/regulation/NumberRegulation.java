package io.moyada.medivh.regulation;

import io.moyada.medivh.util.TypeTag;

/**
 * 数字校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class NumberRegulation extends BaseRegulation {

    private TypeTag typeTag;

    private Object min;

    private Object max;

    NumberRegulation(TypeTag typeTag, Object min, Object max) {
        this.typeTag = typeTag;
        this.min = min;
        this.max = max;
    }

    public TypeTag getTypeTag() {
        return typeTag;
    }

    public Object getMin() {
        return min;
    }

    public Object getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "NumberRegulation{" +
                "typeTag=" + typeTag +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}
