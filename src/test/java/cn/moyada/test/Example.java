package cn.moyada.test;

import io.moyada.medivh.annotation.*;

import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class Example {

    public static InterfaceA test1(@Throw(IllegalStateException.class) ParamA a,
                                  @Return(value = {"null"}) @Nullable String b,
                                  @Return(value = {"test", "true"}, type = SubClass.class) ParamC c) {
        System.out.println("test1");
        return null;
    }

    @Throw
    public static SuperClass test2(@Throw(IllegalStateException.class) ParamB a,
                                   @Throw(message = "size is bound") @SizeRule(max = 20) List<String> b,
                                   @Nullable @NumberRule(max = "4000") Integer c) {
        System.out.println("test2");
        return null;
    }

    @Variable(value = "tmp0")
    public static AbstractClass test3(@Throw(IllegalStateException.class) ParamC a,
                                      String b,
                                      @Throw(message = "something error")
                                      @Nullable @NotBlank @SizeRule(max = 20) String c) {
        System.out.println("test3");
        return null;
    }

    public static void test4(@Throw @Nullable AbstractClass a,
                             @Exclusive @Return @Nullable ParamA b,
                             @Throw @SizeRule(min = 1, max = 10) int[] c) {
        System.out.println("test4");
    }

    @Return("0")
    public static int test5(@Throw @Nullable Integer a,
                            @Return("-1") InterfaceA b,
                            @Throw AbstractClass c) {
        System.out.println("test4");
        return 0;
    }
}
