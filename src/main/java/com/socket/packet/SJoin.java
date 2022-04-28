/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

public class SJoin {
    private String playerName;

    public SJoin(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}

