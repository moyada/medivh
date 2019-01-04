package cn.moyada.test;

import io.moyada.medivh.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class MyApp {

    @Throw
    public Info go(@Throw(value = RuntimeException.class, message = "something error") Args args,
                          @Nullable Info info,
                          @Return({"test", "0.5"}) @NotBlank String name,
                          @SizeRule(max = 10) @Exclusive List<String> params,
                          @NumberRule(min = "0") int num) {
        System.out.println(args);
        System.out.println(info);
        System.out.println(name);
        System.out.println(params);
        System.out.println(num);
        return null;
    }

    class Args {

        @NumberRule(max = "1000")
        int id;

        @NotNull
        HashMap<String, Object> param;

        @Nullable
        @SizeRule(min = 5)
        boolean[] value;
    }

    @Variable("check0")
    static class Info {

        public Info(String name, Double price) {
            this.name = name;
            this.price = price;
        }

        @NotBlank
        String name;

        @Nullable
        @NumberRule(min = "-25.02", max = "200")
        Double price;

        @SizeRule(min = 10, max = 10)
        List<String> extra;
    }
}
