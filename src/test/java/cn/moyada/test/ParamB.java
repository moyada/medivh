package cn.moyada.test;

import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.Nullable;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.SizeRule;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public abstract class ParamB {

    public ParamB(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    @NotNull
    abstract Boolean flag();

    @SizeRule(min = 50)
    String name;

    @Nullable
    @NumberRule(min = "-25.02", max = "200")
    Double price;

    @SizeRule(min = 10, max = 10)
    List<String> extra;
}
