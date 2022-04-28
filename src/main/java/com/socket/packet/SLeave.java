/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

public class SLeave {
    private String playerName;

    public SLeave(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}

