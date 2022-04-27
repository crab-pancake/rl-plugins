/*
 * Decompiled with CFR 0.150.
 */
package com.socket.plugins.specs;

import lombok.Getter;

import java.awt.image.BufferedImage;


public class SpecialIcon {
    @Getter
    private final BufferedImage image;
    @Getter
    private final String text;
    @Getter
    private final long startTime;

    public SpecialIcon(BufferedImage image, String text, long startTime) {
        this.image = image;
        this.text = text;
        this.startTime = startTime;
    }
}

