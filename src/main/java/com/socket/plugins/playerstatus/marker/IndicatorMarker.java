/*
 * Decompiled with CFR 0.150.
 */
package com.socket.plugins.playerstatus.marker;

import com.socket.plugins.playerstatus.gametimer.GameIndicator;
import com.socket.plugins.playerstatus.marker.AbstractMarker;

public class IndicatorMarker
extends AbstractMarker {
    private GameIndicator indicator;

    public IndicatorMarker(GameIndicator indicator) {
        this.indicator = indicator;
    }

    public GameIndicator getIndicator() {
        return this.indicator;
    }
}

