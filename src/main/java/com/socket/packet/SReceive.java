/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

import com.socket.org.json.JSONObject;

public class SReceive {
    private final JSONObject payload;

    public SReceive(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getPayload() {
        return this.payload;
    }
}

