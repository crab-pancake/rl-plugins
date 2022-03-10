/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 */
package com.socket.plugins.socketplanks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(value="planks")
public interface SocketPlanksConfig
extends Config {
    @ConfigItem(keyName="splitTimer", name="Split Timer", description="Displays split timer")
    default boolean splitTimer() {
        return true;
    }
}

