/*
 * Decompiled with CFR 0.150.
 */
package com.socket.packet;

import java.util.List;

public class SocketMembersUpdate {
    private List<String> members;

    public SocketMembersUpdate(List<String> members) {
        this.members = members;
    }

    public List<String> getMembers() {
        return this.members;
    }
}

