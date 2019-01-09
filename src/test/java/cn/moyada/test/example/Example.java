package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

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

        @Min(0)
        @Max(300)
        private int age;
    }
}
