package cn.moyada.test;

import io.moyada.medivh.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class MyApp {

    @Verify
    public static Info go(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check(throwable = false, returnValue = {"test", "0.5"}) String name,
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

        @SizeRule(min = 50)
        String name;

        @Nullable
        @NumberRule(min = "-25.02", max = "200")
        Double price;

        @SizeRule(min = 10, max = 10)
        List<String> extra;
    }
}
