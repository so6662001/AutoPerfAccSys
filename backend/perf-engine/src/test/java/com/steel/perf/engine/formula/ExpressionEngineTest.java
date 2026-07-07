package com.steel.perf.engine.formula;

import com.steel.perf.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 公式引擎（沙箱）测试。 */
class ExpressionEngineTest {

    private final ExpressionEngine engine = new ExpressionEngine();

    @Test
    void arithmetic() {
        assertEquals(6000d, engine.evaluate("6000 * 1.0", Map.of()), 1e-9);
        assertEquals(14d, engine.evaluate("2 + 3 * 4", Map.of()), 1e-9);
        assertEquals(20d, engine.evaluate("(2 + 3) * 4", Map.of()), 1e-9);
    }

    @Test
    void variables() {
        // 超扑越挂价利润个人所得 = (成交-挂牌)*吨位*分配系数 = 10*800*0.7 = 5600
        Map<String, Double> vars = Map.of("deal", 4010d, "list", 4000d, "qty", 800d, "alloc", 0.7d);
        assertEquals(5600d, engine.evaluate("(deal - list) * qty * alloc", vars), 1e-9);
    }

    @Test
    void functions() {
        assertEquals(50d, engine.evaluate("MIN(u, 50)", Map.of("u", 120d)), 1e-9);   // 吨利润封顶
        assertEquals(4375.13d, engine.evaluate("ROUND(x, 2)", Map.of("x", 4375.126d)), 1e-9);
        assertEquals(0d, engine.evaluate("IF(r < 0.6, 0, 1)", Map.of("r", 0.5d)), 1e-9);
        assertEquals(1d, engine.evaluate("IF(r < 0.6, 0, 1)", Map.of("r", 0.9d)), 1e-9);
    }

    @Test
    void chineseVariables() {
        // 支持中文变量名
        assertEquals(4375d, engine.evaluate("预收 * 日利率 * 天数 * 分配",
                Map.of("预收", 500000d, "日利率", 0.0005d, "天数", 25d, "分配", 0.7d)), 1e-6);
    }

    @Test
    void rejectUnknownVariable() {
        assertThrows(BizException.class, () -> engine.evaluate("a + b", Map.of("a", 1d)));
    }

    @Test
    void rejectUnknownFunction() {
        assertThrows(BizException.class, () -> engine.evaluate("EVIL(1)", Map.of()));
    }

    @Test
    void rejectDivideByZero() {
        assertThrows(BizException.class, () -> engine.evaluate("1 / 0", Map.of()));
    }

    @Test
    void rejectTrailingGarbage() {
        assertThrows(BizException.class, () -> engine.evaluate("1 + 2 )", Map.of()));
    }
}
