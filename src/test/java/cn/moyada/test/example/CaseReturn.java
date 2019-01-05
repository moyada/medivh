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

    public boolean exist(@Return("false") @NotNull String name,
                         @NumberRule(min = "0") Double price) {
        System.out.println(name);
        System.out.println(price);
        return true;
    }

    public int count(@Return("0") @NumberRule(min = "0") Double price) {
        return 1000;
    }

    public Capacity get(@Return("null") @NotBlank String name,
                        @Return({"test", "true"}) @NumberRule(min = "0") byte type) {
        return new Capacity();
    }

    public Product find(@Return(type = Item.class) @NotBlank String name) {
        return null;
    }

    static class Item implements Product {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public List<Capacity> total() {
            return null;
        }
    }
}
