package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class CaseReturn {

    public boolean returnPrimitive(@Return("false") @NotNull String name,
                                   @Return("true") @NumberRule(min = "0.0") double price,
                                   boolean putaway) {
        System.out.println("returnPrimitive");
        return true;
    }

    public Integer returnBasic(@Return("0") Product product,
                               @Throw(message = "null") @SizeRule(min = 0) List<String> param) {
        System.out.println("returnBasic");
        return -1;
    }

    public Capacity returnObject(@Return({"test", "true"})  @NotNull String name,
                                 @Return @NumberRule(min = "0") Byte type) {
        System.out.println("returnObject");
        return new Capacity();
    }

    public Product returnInterface(@Return(type = Item.class) @NotBlank String name) {
        System.out.println("returnInterface");
        return null;
    }

    public Product useStaticMethod(@Return(type = CaseReturn.class, staticMethod = "getProduct") @SizeRule(min = 1) String name,
                                   @Return(value = "test", type = CaseReturn.class, staticMethod = "getProduct") @NotNull Integer id) {
        System.out.println("useStaticMethod");
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
