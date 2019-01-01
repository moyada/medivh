package cn.moyada.test;

import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.Nullable;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.SizeRule;

import java.util.HashMap;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class ParamA {

    String name;

    @NumberRule(max = "1000")
    int getId() {
        return name.hashCode();
    }

    @NotNull
    HashMap<String, Object> param;

    @Nullable
    @SizeRule(min = 5)
    boolean[] value;
}
