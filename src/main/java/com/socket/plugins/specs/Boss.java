/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package com.socket.plugins.specs;

import com.google.common.collect.Sets;

import java.util.Set;

enum Boss {
    ABYSSAL_SIRE(5886, 5887, 5888, 5889, 5890, 5891, 5908),
    CALLISTO(6503, 6609),
    CERBERUS(5862, 5863, 5866),
    CHAOS_ELEMENTAL(2054, 6505),
    CORPOREAL_BEAST(319),
    GENERAL_GRAARDOR(2215, 6494),
    GIANT_MOLE(5779, 6499),
    KALPHITE_QUEEN(128, 963, 965, 4303, 4304, 6500, 6501),
    KING_BLACK_DRAGON(239, 2642, 6502),
    KRIL_TSUTSAROTH(3129, 6495),
    VENENATIS(6504, 6610),
    VETION(6611, 6612),
    MAIDEN(8360,8361,8362,8363,10822,10823,10824),
    SOTETSEG(8387, 8388, 10867, 10868, 10864, 10865),
    XARPUS(8340,8341,10770,10771,10772,10773),
    TEKTON(7540,7541,7542,7543,7544,7545),
    OLM(7552,7555);  // TODO: check maiden tek xarpus olm

    private final Set<Integer> ids;

    Boss(Integer ... ids) {
        this.ids = Sets.newHashSet(ids);
    }

    static Boss getBoss(int id) {
        for (final Boss boss : values()) {
            if (boss.ids.contains(id)) return boss;
        }
        return null;
    }

    public Set<Integer> getIds() {
        return this.ids;
    }

    public String toString() {
        return "Boss." + this.name() + " (ids=" + this.getIds() + ")";
    }
}

