package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

/**
 * @author xueyikang
 * @since 1.0
 **/
@Throw
public class CaseInheritance {

    public boolean save(@NotNull String name,
                        @Throw(NumberFormatException.class) @NumberRule(min = "0.0") double price,
                        boolean putaway) {
        System.out.println(name);
        System.out.println(price);
        System.out.println(putaway);
        return true;
    }

    @Return("null")
    public Capacity get(@NotBlank String name,
                        @Return({"test", "true"}) @NumberRule(min = "0") byte type) {
        return new Capacity();
    }

    @Variable("tmp0")
    @Throw(value = IllegalStateException.class, message = "price error")
    public void update(Product product,
                       @NumberRule(min = "0.0") Double price) {
        System.out.println(product);
        System.out.println(price);
    }

    public boolean exist(Product product) {
        return true;
    }

    @Exclusive
    public int count(Product product) {
        return 1000;
    }
}
