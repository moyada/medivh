package io.moyada.medivh.core;

/**
 * 规则动作信息
 * @author xueyikang
 * @since 1.0
 **/
public class ActionData {

    // 动作模式
    private byte actionMode;

    // 类名
    private String className;

    // 信息
    private String info;

    public ActionData(byte actionMode, String className, String info) {
        this.actionMode = actionMode;
        this.className = className;
        this.info = info;
    }

    public byte getActionMode() {
        return actionMode;
    }

    public String getClassName() {
        return className;
    }

    public String getInfo() {
        return info;
    }
}
