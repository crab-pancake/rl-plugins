/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

import com.socket.org.json.JSONObject;

public class SocketBroadcastPacket {
    private final JSONObject payload;

    public SocketBroadcastPacket(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getPayload() {
        return this.payload;
    }
}

