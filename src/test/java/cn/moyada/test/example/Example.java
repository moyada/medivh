package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotBlank;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.Return;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class Example {

    public boolean save(@Return("false") Person name) {
        System.out.println(name);
        return true;
    }

    class Person {

        @NotBlank
        private String name;

        @NumberRule(min = "0", max = "500")
        private int age;
    }
}
