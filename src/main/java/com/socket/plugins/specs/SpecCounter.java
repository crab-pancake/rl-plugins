/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.ui.overlay.infobox.Counter
 */
package com.socket.plugins.specs;

import net.runelite.client.ui.overlay.infobox.Counter;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

class SpecCounter
extends Counter {
    private final SpecPlugin plugin;
    private SpecialWeapon weapon;
    private final Map<String, Integer> partySpecs = new HashMap<>();

    SpecCounter(BufferedImage image, SpecPlugin plugin, int hitValue, SpecialWeapon weapon) {
        super(image, plugin, hitValue);
        this.plugin = plugin;
        this.weapon = weapon;
    }

    void addHits(double hit) {
        int count = getCount();
        setCount(count + (int)hit);
    }

    public String getTooltip() {
        int hitValue = getCount();
        if (partySpecs.isEmpty()) {
            return buildTooltip(hitValue);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(buildTooltip(hitValue));
        for (Map.Entry<String, Integer> entry : partySpecs.entrySet()) {
            stringBuilder.append("</br>").append(entry.getKey() == null ? "You" : entry.getKey()).append(": ").append(this.buildTooltip(entry.getValue()));
        }
        return stringBuilder.toString();
    }

    private String buildTooltip(int hitValue) {
        if (!weapon.isDamage()) {
            if (hitValue == 1) {
                return weapon.getName() + " special has hit once.";
            }
            return weapon.getName() + " special has hit " + hitValue + " times.";
        }
        return weapon.getName() + " special has hit " + hitValue + " total.";
    }

    Map<String, Integer> getPartySpecs() {
        return partySpecs;
    }
}

