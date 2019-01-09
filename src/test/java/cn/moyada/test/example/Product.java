package cn.moyada.test.example;

import io.moyada.medivh.annotation.*;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public interface Product {

    @Nullable
    @Min(-5)
    @Max(80)
    long getId();

    String getName();

    @Nullable
    @NotBlank
    String getType();

    @Nullable
    @Size(min = 1, max = 10)
    List<Capacity> getStore();
}
