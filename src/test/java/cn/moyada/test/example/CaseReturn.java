package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotBlank;
import io.moyada.medivh.annotation.NotNull;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.Return;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class CaseReturn {

    public boolean returnPrimitive(@Return("false") @NotNull String name,
                                   Double price) {
        System.out.println(name);
        System.out.println(price);
        return true;
    }

    public Integer returnBasic(@Return("0") @NumberRule(min = "0") Double price) {
        return null;
    }

    public Capacity returnObject(@Return("null") @NotBlank String name,
                                 @Return({"test", "true"}) @NumberRule(min = "0") byte type) {
        return new Capacity();
    }

    public Product returnInterface(@Return(type = Item.class) @NotBlank String name) {
        return null;
    }

    public Product useStaticMethod(@Return(type = CaseReturn.class, staticMethod = "getProduct") @NotBlank String name,
                                   @Return(value = "test", type = CaseReturn.class, staticMethod = "getProduct") @NotNull Integer id) {
        return null;
    }

    public static Product getProduct() {
        return new Item();
    }

    public static Product getProduct(String name) {
        return new Item(name);
    }

    static class Item implements Product {

        private String name;

        public Item() {
        }

        public Item(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public List<Capacity> getStore() {
            return null;
        }
    }
}
