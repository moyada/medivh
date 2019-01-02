package cn.moyada.test;

import io.moyada.medivh.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class MyApp {

    public static Info go(@Throw(RuntimeException.class) Args args,
                          @Throw(message = "something error") @Nullable Info info,
                          @Return({"test", "0.5"}) String name,
                          int num) {
        System.out.println(args);
        System.out.println(info);
        System.out.println(name);
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
