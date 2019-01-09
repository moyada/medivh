package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

/**
 * @author xueyikang
 * @since 1.0
 **/
@Throw
public class CaseInherit {

    public boolean customRule(Product product, Capacity capacity) {
        System.out.println("customRule");
        return true;
    }

    @Return({"test", "true"})
    public Capacity useReturn(@NotBlank String name,
                              @Throw Counter counter) {
        System.out.println("useReturn");
        return null;
    }

    @Variable("tmp0")
    @Throw(value = UnsupportedOperationException.class)
    public void excludeParam(@Exclusive Product product,
                             @Size(min = 5) int[] ids) {
        System.out.println("excludeParam");
    }

    @Exclusive
    public int excludeMethod(@Return("-1") Product product) {
        return 0;
    }
}
