package io.moyada.medivh.core;

/**
 * 语法类型
 * @author xueyikang
 * @since 1.0
 **/
public enum TypeTag {

    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    CHAR,
    NE, // 不等于
    EQ, // 等于
    GT, // 大于
    LT, // 小于
    PLUS, // + 操作
    BOT, // null
    CLASS, // 对象
    ;
}
