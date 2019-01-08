package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.SizeRule;
import io.moyada.medivh.annotation.Throw;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class CaseThrow {

    public boolean hasReturn(@Throw @NotNull String name,
                             @Throw(NumberFormatException.class) @NumberRule(min = "0.0") double price,
                             boolean putaway) {
        System.out.println("hasReturn");
        return true;
    }

    public void nonReturn(@Throw(value = IllegalStateException.class, message = "unknown error") Product product,
                          @Throw(message = "price error") @SizeRule(min = 0, max = 20) List<String> param) {
        System.out.println("nonReturn");
    }
}
