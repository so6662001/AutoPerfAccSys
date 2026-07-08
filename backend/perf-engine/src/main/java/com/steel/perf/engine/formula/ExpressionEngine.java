package com.steel.perf.engine.formula;

import com.steel.perf.common.exception.BizException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 公式引擎（平台基础能力，沙箱安全）。
 * 递归下降解析器，仅支持：数字、变量、+ - * / 、括号、比较(&lt; &gt; &lt;= &gt;= == !=)、
 * 白名单函数 MIN/MAX/ROUND/CEIL/FLOOR/ABS/IF。
 * 严禁 eval 任意代码 / 反射 / IO，杜绝注入。变量缺失即报错。
 */
public class ExpressionEngine {

    public double evaluate(String expression, Map<String, Double> vars) {
        if (expression == null || expression.isBlank()) {
            throw new BizException("公式为空");
        }
        Parser p = new Parser(expression, vars == null ? Map.of() : vars);
        double v = p.parseExpression();
        p.expectEnd();
        return v;
    }

    /** 递归下降解析器（有状态，非线程安全，按次创建）。 */
    private static final class Parser {
        private final String src;
        private final Map<String, Double> vars;
        private int pos;

        Parser(String src, Map<String, Double> vars) {
            this.src = src;
            this.vars = vars;
            this.pos = 0;
        }

        // expression := comparison
        double parseExpression() {
            return parseComparison();
        }

        double parseComparison() {
            double left = parseAdditive();
            skipWs();
            String op = matchCompareOp();
            if (op == null) {
                return left;
            }
            double right = parseAdditive();
            return switch (op) {
                case "<" -> left < right ? 1d : 0d;
                case ">" -> left > right ? 1d : 0d;
                case "<=" -> left <= right ? 1d : 0d;
                case ">=" -> left >= right ? 1d : 0d;
                case "==" -> left == right ? 1d : 0d;
                case "!=" -> left != right ? 1d : 0d;
                default -> throw new BizException("非法比较运算符: " + op);
            };
        }

        double parseAdditive() {
            double v = parseMultiplicative();
            while (true) {
                skipWs();
                char c = peek();
                if (c == '+') { pos++; v += parseMultiplicative(); }
                else if (c == '-') { pos++; v -= parseMultiplicative(); }
                else { break; }
            }
            return v;
        }

        double parseMultiplicative() {
            double v = parseUnary();
            while (true) {
                skipWs();
                char c = peek();
                if (c == '*') { pos++; v *= parseUnary(); }
                else if (c == '/') {
                    pos++;
                    double d = parseUnary();
                    if (d == 0d) { throw new BizException("公式除零"); }
                    v /= d;
                } else { break; }
            }
            return v;
        }

        double parseUnary() {
            skipWs();
            if (peek() == '-') { pos++; return -parseUnary(); }
            if (peek() == '+') { pos++; return parseUnary(); }
            return parsePrimary();
        }

        double parsePrimary() {
            skipWs();
            char c = peek();
            if (c == '(') {
                pos++;
                double v = parseExpression();
                skipWs();
                expect(')');
                return v;
            }
            if (Character.isDigit(c) || c == '.') {
                return parseNumber();
            }
            if (isIdentStart(c)) {
                return parseIdentifierOrCall();
            }
            throw new BizException("公式非法字符 '" + c + "' @" + pos);
        }

        double parseNumber() {
            int start = pos;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
                pos++;
            }
            try {
                return Double.parseDouble(src.substring(start, pos));
            } catch (NumberFormatException e) {
                throw new BizException("非法数字: " + src.substring(start, pos));
            }
        }

        double parseIdentifierOrCall() {
            int start = pos;
            while (pos < src.length() && (isIdentPart(src.charAt(pos)))) {
                pos++;
            }
            String name = src.substring(start, pos);
            skipWs();
            if (peek() == '(') {
                pos++;
                List<Double> args = new java.util.ArrayList<>();
                skipWs();
                if (peek() != ')') {
                    args.add(parseExpression());
                    skipWs();
                    while (peek() == ',') {
                        pos++;
                        args.add(parseExpression());
                        skipWs();
                    }
                }
                expect(')');
                return callFunction(name.toUpperCase(Locale.ROOT), args);
            }
            // 变量
            Double val = vars.get(name);
            if (val == null) {
                throw new BizException("公式引用未知变量: " + name);
            }
            return val;
        }

        double callFunction(String name, List<Double> a) {
            return switch (name) {
                case "MIN" -> { requireArgs(name, a, 2); yield Math.min(a.get(0), a.get(1)); }
                case "MAX" -> { requireArgs(name, a, 2); yield Math.max(a.get(0), a.get(1)); }
                case "ABS" -> { requireArgs(name, a, 1); yield Math.abs(a.get(0)); }
                case "CEIL" -> { requireArgs(name, a, 1); yield Math.ceil(a.get(0)); }
                case "FLOOR" -> { requireArgs(name, a, 1); yield Math.floor(a.get(0)); }
                case "ROUND" -> {
                    if (a.size() == 1) { yield Math.round(a.get(0)); }
                    requireArgs(name, a, 2);
                    double f = Math.pow(10, a.get(1).intValue());
                    yield Math.round(a.get(0) * f) / f;
                }
                case "IF" -> { requireArgs(name, a, 3); yield a.get(0) != 0d ? a.get(1) : a.get(2); }
                default -> throw new BizException("公式引用未知函数: " + name);
            };
        }

        void requireArgs(String name, List<Double> a, int n) {
            if (a.size() != n) {
                throw new BizException("函数 " + name + " 需要 " + n + " 个参数，实际 " + a.size());
            }
        }

        String matchCompareOp() {
            skipWs();
            if (pos + 1 < src.length()) {
                String two = src.substring(pos, pos + 2);
                if (two.equals("<=") || two.equals(">=") || two.equals("==") || two.equals("!=")) {
                    pos += 2;
                    return two;
                }
            }
            char c = peek();
            if (c == '<' || c == '>') {
                pos++;
                return String.valueOf(c);
            }
            return null;
        }

        void expect(char c) {
            skipWs();
            if (peek() != c) {
                throw new BizException("公式缺少 '" + c + "' @" + pos);
            }
            pos++;
        }

        void expectEnd() {
            skipWs();
            if (pos != src.length()) {
                throw new BizException("公式存在多余字符 @" + pos + ": " + src.substring(pos));
            }
        }

        char peek() {
            return pos < src.length() ? src.charAt(pos) : '\0';
        }

        void skipWs() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        boolean isIdentStart(char c) {
            return Character.isLetter(c) || c == '_' || c == '$' || c > 127;
        }

        boolean isIdentPart(char c) {
            return isIdentStart(c) || Character.isDigit(c);
        }
    }
}
