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
    public Boolean run(Args args,
                    @Return("null") @NotBlank String name,
                    @Return("false") @Min(1) int num) {
        System.out.println("process");
        return true;
    }

    class Args {

        @Max(1000) int id;

        @NotBlank String name;

        @Nullable @DecimalMin(-25.02) @DecimalMax(200) Double price;

        @Size(min = 10, max = 10) String[] values;

        @NotNull HashMap<String, Object> param;

        @Size(max = 5) List<String> extra;
    }
}
