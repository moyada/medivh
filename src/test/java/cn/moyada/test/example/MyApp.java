package cn.moyada.test.example;

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
                    @Return("null") @Min(1) int num) {
        // process
        return new Info();
    }

    class Args {

        @Max(1000) int id;

        @NotNull HashMap<String, Object> param;

        @Nullable @Size(max = 5) boolean[] value;
    }

    static class Info {

        @Size(max = 50) String name;

        @Nullable @DecimalMin(-25.02) @DecimalMax(200) Double price;

        @Size(min = 10, max = 10) List<String> extra;

        public Info() {
        }

        Info(String name, Double price) {
            this.name = name;
            this.price = price;
        }
    }
}
