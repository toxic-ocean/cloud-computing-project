package log;

import com.datastax.driver.core.TupleType;

import java.util.HashMap;
import java.util.Map;

public class UdfTest {

    public static Map<String, Integer> q3(Map<String, Integer> input) {
        Integer max = Integer.MIN_VALUE;
        String data = "";
        for (String k : input.keySet()) {
            Integer tmp = input.get(k);
            if (tmp > max) { max = tmp; data = k; }
        }
        Map<String, Integer> mm = new HashMap<>();
        mm.put(data, max);
        return mm;
    }
}
