package com.steel.perf.rule;

import com.steel.perf.common.exception.BizException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 发布硬校验：评分卡权重合计必须 = 100%，否则拦截发布。
 */
public class PublishValidator {

    /**
     * @param scorecardWeightSums 评分卡编码 -> 权重合计
     */
    public void validateWeights(Map<String, Integer> scorecardWeightSums) {
        List<String> bad = new ArrayList<>();
        if (scorecardWeightSums != null) {
            for (Map.Entry<String, Integer> e : scorecardWeightSums.entrySet()) {
                if (e.getValue() == null || e.getValue() != 100) {
                    bad.add(e.getKey() + " " + e.getValue() + "%");
                }
            }
        }
        if (!bad.isEmpty()) {
            throw new BizException("发布被拦截：评分卡权重合计必须=100%：" + String.join("、", bad));
        }
    }
}
