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
    public Info run(Args args,
                    @Nullable Info info,
                    @Return({"test", "20"}) @NotBlank String name,
                    @Return("null") @NumberRule(min = "1") int num) {
        // process
        return new Info();
    }

    class Args {

        @NumberRule(max = "1000") int id;

        @NotNull HashMap<String, Object> param;

        @Nullable @SizeRule(min = 5) boolean[] value;
    }

    class Info {

        @SizeRule(min = 50) String name;

        @Nullable @NumberRule(min = "-25.02", max = "200") Double price;

        @SizeRule(min = 10, max = 10) List<String> extra;

        public Info() {
        }

        Info(String name, Double price) {
            this.name = name;
            this.price = price;
        }
    }
}
