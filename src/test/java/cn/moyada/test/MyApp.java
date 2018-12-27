package cn.moyada.test;

import io.moyada.medivh.annotation.Check;
import io.moyada.medivh.annotation.Rule;
import io.moyada.medivh.annotation.Verify;

import java.util.HashMap;
import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class MyApp {

    @Verify
    public static void go(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check String name,
                    int num) {
        System.out.println(args);
        System.out.println(info);
        System.out.println(name);
        System.out.println(num);
    }

    class Args {

        @Rule(min = 10, max = 2000)
        int id;

        @Rule
        HashMap<String, Object> param;

        @Rule(maxLength = 5, nullable = true)
        String[] value;
    }

    class Info {

        @Rule(maxLength = 20)
        String name;

        @Rule(min = -250, max = 500, nullable = true)
        Double price;

        @Rule(maxLength = 10)
        List<String> extra;
    }
}
