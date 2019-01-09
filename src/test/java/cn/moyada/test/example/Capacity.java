package cn.moyada.test.example;

import io.moyada.medivh.annotation.Size;

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

    @Size(min = 1, max = 50)
    private String type;

    @Size(max = 13)
    private byte[] getTypes() {
        return new byte[0];
    }

    @Size(min = 2)
    private List<Counter> counters;

    @Size(min = 10, max = 10)
    public Map<String, Integer> getEntry() {
        return new HashMap<String, Integer>();
    }
}
