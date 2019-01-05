package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.Throw;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class CaseThrow {

    public boolean save(@Throw @NotNull String name,
                        @Throw(NumberFormatException.class) @NumberRule(min = "0.0") double price,
                        boolean putaway) {
        System.out.println(name);
        System.out.println(price);
        System.out.println(putaway);
        return true;
    }

    public void update(@Throw Product product,
                       @Throw(message = "price error") @NumberRule(min = "0.0") Double price) {
        System.out.println(product);
        System.out.println(price);
    }
}
