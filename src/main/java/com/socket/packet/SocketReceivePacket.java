/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

import com.socket.org.json.JSONObject;

public class SocketReceivePacket {
    private JSONObject payload;

    public SocketReceivePacket(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getPayload() {
        return this.payload;
    }
}

