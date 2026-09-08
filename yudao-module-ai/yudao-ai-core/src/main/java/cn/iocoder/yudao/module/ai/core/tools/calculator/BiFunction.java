package cn.iocoder.yudao.module.ai.core.tools.calculator;

interface BiFunction<T, U, R> {
    R apply(T t, U u);
}