package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class CaseCombine {

    public boolean returnPrimitive(@Return("false") @NotNull String name,
                                   @Throw @NumberRule(min = "0") Double price) {
        System.out.println(name);
        System.out.println(price);
        return true;
    }

    public Capacity returnObject(@Return("null") @NotBlank String name,
                                 @Throw @NumberRule(min = "0") byte type,
                                 @Return({"null", "false"}) @NotNull Double price) {
        return new Capacity();
    }

    public Product useStaticMethod(@Return(type = CaseReturn.class, staticMethod = "getProduct") @NotBlank String name,
                                   @Throw(value = IllegalStateException.class, message = "id error") @NumberRule(min = "0") Integer id) {
        return null;
    }
}
