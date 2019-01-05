package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.Variable;

/**
 * @author xueyikang
 * @since 1.0
 **/
@Variable("check0")
public class Param {

    @NotNull
    private String name;

    @NotNull
    private Object value;
}
