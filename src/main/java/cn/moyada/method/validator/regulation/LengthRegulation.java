package cn.moyada.method.validator.regulation;

/**
 * 长度校验规则
 * @author xueyikang
 * @since 1.0
 **/
public class LengthRegulation extends BaseRegulation {

    public final static byte STRING = 0;
    public final static byte ARRAY = 1;

    public final static int EXCLUDE = -1;

    private int length;

    private byte type;

    public LengthRegulation(int length, byte type) {
        this.length = length;
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public boolean isStr() {
        return type == STRING;
    }

    public boolean isArr() {
        return type == ARRAY;
    }
}
