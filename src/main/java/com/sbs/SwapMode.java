package com.sbs;

import java.util.function.Predicate;

public interface SwapMode {
    default boolean strict() {
        return true;
    }

    default boolean checkShift() {
        return false;
    }

    default Predicate<String> checkTarget() {
        return (s) -> {
            return true;
        };
    }

    String getOption();
}