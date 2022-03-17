package com.sbs;


import java.util.function.Predicate;

public enum SpellbookSwapMode implements SwapMode {
    CAST("Cast"),
    STANDARD("Standard"),
    ANCIENT("Ancient"),
    ARCEUUS("Arceuus");

    private final String option;

    public String toString() {
        return this.option;
    }

    public boolean checkShift() {
        return true;
    }

    public Predicate<String> checkTarget() {
        return (target) -> {
            return target.startsWith("spellbook swap");
        };
    }

    public String getOption() {
        return this.option;
    }

    private SpellbookSwapMode(String option) {
        this.option = option;
    }
}
