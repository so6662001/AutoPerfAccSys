package com.steel.perf.rule;

import com.steel.perf.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublishValidatorAndDiffTest {

    private final PublishValidator validator = new PublishValidator();
    private final ParamDiff paramDiff = new ParamDiff();

    @Test
    void weightSumMustBe100() {
        validator.validateWeights(Map.of("销售评分卡", 100, "采购评分卡", 100)); // ok
        assertThrows(BizException.class,
                () -> validator.validateWeights(Map.of("销售评分卡", 90)));
    }

    @Test
    void diffParams() {
        Map<String, String> a = Map.of("权重", "40/20/20/20", "预收日利率", "5‱", "同项", "x");
        Map<String, String> b = Map.of("权重", "50/20/15/15", "预收日利率", "6‱", "同项", "x");
        List<ParamDiff.DiffItem> items = paramDiff.diff(a, b);
        assertEquals(2, items.size()); // 权重、预收日利率 有差异；同项一致不计
    }
}
