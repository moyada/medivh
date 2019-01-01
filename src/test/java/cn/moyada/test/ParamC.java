package cn.moyada.test;

import io.moyada.medivh.annotation.NumberRule;
import io.moyada.medivh.annotation.SizeRule;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public interface ParamC {

    @NumberRule(max = "20")
    int getAge();

    @SizeRule(min = 10)
    List getItems();
}
