package io.moyada.medivh.regulation;

import io.moyada.medivh.util.TypeTag;

/**
 * 数字校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class EqualsRegulation extends BaseRegulation {

    private TypeTag typeTag;

    private Object value;

    EqualsRegulation(TypeTag typeTag, Object value) {
        this.typeTag = typeTag;
        this.value = value;
    }

    public TypeTag getTypeTag() {
        return typeTag;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EqualsRegulation{" +
                "typeTag=" + typeTag +
                ", value=" + value +
                '}';
    }
}
