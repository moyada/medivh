package cn.moyada.function.validator.core;

/**
 * 字符串校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class StringValidation extends BaseValidation {

    public final static int EXCLUDE = -1;

    private int length;

    public StringValidation(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}
