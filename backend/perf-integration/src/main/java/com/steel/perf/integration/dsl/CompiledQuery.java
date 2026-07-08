package com.steel.perf.integration.dsl;

import java.util.List;

/**
 * 编译产物：参数化只读 SQL + 参数绑定顺序。
 * SQL 中仅含命名参数占位（:name），值由调用方按 paramOrder 绑定，杜绝拼接注入。
 */
public record CompiledQuery(String sql, List<String> paramOrder) {
}
