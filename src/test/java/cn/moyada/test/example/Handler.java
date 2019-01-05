package cn.moyada.test.example;

import io.moyada.medivh.annotation.NotBlank;
import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.SizeRule;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public interface Handler {

    @NumberRule(min = "0")
    int getId();

    @NotBlank
    String getType();

    @SizeRule(min = 0, max = 20)
    List<Param> getParams();
}
