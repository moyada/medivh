package cn.moyada.test.example;

import io.moyada.medivh.annotation.SizeRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class Capacity {

    public Capacity() {
    }

    public Capacity(String type, boolean counters) {
        this.type = type;
    }

    @SizeRule(min = 0, max = 50)
    private String type;

    @SizeRule(min = 1)
    private byte[] getTypes() {
        return new byte[0];
    }

    @SizeRule(max = 200)
    private List<Counter> counters;

    @SizeRule(min = 10, max = 10)
    public Map<String, Integer> getEntry() {
        return new HashMap<String, Integer>();
    }
}
