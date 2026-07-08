package com.steel.perf.engine.coefficient;

import com.steel.perf.common.exception.BizException;

import java.util.List;

/**
 * 达成率 / 完成率 映射引擎（平台基础能力 P0）。
 * 支持：达成率 -> 系数（超扑越综合达成率定档、岳洋通团队系数）
 * 与 达成率 -> 固定档位金额（超扑越基本工资 2000/2500/3000/3500）。
 * 档位按 upper 升序，取第一个 rate ≤ upper 的档；最后一档 upper=null 兜底。
 */
public class CoefficientMapper {

    /**
     * @param rate  达成率（百分数，如 108.2 表示 108.2%）
     * @param bands 档位（按 upper 升序）
     * @return 命中档位的 value（系数或金额）
     */
    public double map(double rate, List<Band> bands) {
        if (bands == null || bands.isEmpty()) {
            throw new BizException("映射档位为空");
        }
        for (Band b : bands) {
            if (b.getUpper() == null || rate <= b.getUpper()) {
                return b.getValue();
            }
        }
        return bands.get(bands.size() - 1).getValue();
    }
}
