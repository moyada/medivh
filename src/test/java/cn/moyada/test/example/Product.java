package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotBlank;
import io.moyada.medivh.annotation.Nullable;
import io.moyada.medivh.annotation.SizeRule;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public interface Product {

    String getName();

    @Nullable
    @NotBlank
    String getType();

    @Nullable
    @SizeRule(min = 0)
    List<Capacity> getStore();
}
