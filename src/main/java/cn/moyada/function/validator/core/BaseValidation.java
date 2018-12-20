package cn.moyada.function.validator.core;

/**
 * 校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class BaseValidation {

    private boolean nullable;

    private boolean primitive;

    public BaseValidation() {
        this.primitive = false;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }
}
