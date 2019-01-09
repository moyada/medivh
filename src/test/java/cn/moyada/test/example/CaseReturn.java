package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class CaseReturn {

    public boolean returnPrimitive(@Return("false") @NotNull String name,
                                   @Return("true") @DecimalMin(0.5) double price,
                                   boolean putaway) {
        System.out.println("returnPrimitive");
        return true;
    }

    public Integer returnBasic(@Return("0") Product product,
                               @Throw(message = "null") @Size(min = 1) List<String> param) {
        System.out.println("returnBasic");
        return -1;
    }

    public Capacity returnObject(@Return({"test", "true"})  @NotNull String name,
                                 @Return @Min(0) Byte type) {
        System.out.println("returnObject");
        return new Capacity();
    }

    public Product returnInterface(@Return(type = Item.class) @NotBlank String name) {
        System.out.println("returnInterface");
        return null;
    }

    public Product useStaticMethod(@Return(type = CaseReturn.class, staticMethod = "getProduct") @Size(min = 3) String name,
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
        public long getId() {
            return 0;
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
