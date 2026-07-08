package com.steel.perf.rule;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 版本参数差异对比（时间轴 diff）。
 */
public class ParamDiff {

    public record DiffItem(String key, String oldVal, String newVal) {
    }

    public List<DiffItem> diff(Map<String, String> a, Map<String, String> b) {
        List<DiffItem> items = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        if (a != null) keys.addAll(a.keySet());
        if (b != null) keys.addAll(b.keySet());
        for (String k : keys) {
            String av = a == null ? null : a.get(k);
            String bv = b == null ? null : b.get(k);
            if (!java.util.Objects.equals(av, bv)) {
                items.add(new DiffItem(k, av, bv));
            }
        }
        return items;
    }
}
