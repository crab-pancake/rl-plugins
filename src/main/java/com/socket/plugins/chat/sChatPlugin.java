/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provides
 *  javax.inject.Inject
 *  net.runelite.api.ChatMessageType
 *  net.runelite.api.Client
 *  net.runelite.api.VarClientStr
 *  net.runelite.api.events.BeforeRender
 *  net.runelite.api.events.VarClientStrChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.chat.ChatMessageManager
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.input.KeyListener
 *  net.runelite.client.input.KeyManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.util.ColorUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.socket.plugins.chat;

import com.google.inject.Provides;
import com.socket.org.json.JSONArray;
import com.socket.org.json.JSONObject;
import com.socket.packet.SocketBroadcastPacket;
import com.socket.packet.SocketReceivePacket;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

@PluginDescriptor(name="Socket - Chat", description="Chat over socket", tags={"Socket", "chat"})
public class sChatPlugin
extends Plugin
implements KeyListener {
    private static final Logger log = LoggerFactory.getLogger(sChatPlugin.class);
    @Inject
    Client client;
    @Inject
    private KeyManager keyManager;
    @Inject
    sChatConfig config;
    @Inject
    private ClientThread clientThread;
    private boolean tradeActive = false;
    private boolean typing = false;
    private boolean lastTypingState = false;
    @Inject
    private EventBus eventBus;
    @Inject
    private ChatMessageManager chatMessageManager;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
    SimpleDateFormat formatterr = new SimpleDateFormat("HH:mm");
    SimpleDateFormat formatterrr = new SimpleDateFormat("MM/dd");

    @Provides
    sChatConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(sChatConfig.class);
    }

    protected void startUp() throws Exception {
        this.keyManager.registerKeyListener(this);
    }

    public void keyTyped(KeyEvent e) {
    }

    @Subscribe
    public void onVarClientStrChanged(VarClientStrChanged event) throws InterruptedException {
        this.removeHotkey();
    }

    private void removeHotkey() throws InterruptedException {
        String typedText = this.client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
        if (typedText.length() > 0) {
            String subTypedText = typedText.substring(0, typedText.length() - 1);
            String x = KeyEvent.getKeyText(this.config.hotkey().getKeyCode());
            char a = (char)KeyEvent.getExtendedKeyCodeForChar(typedText.substring(typedText.length() - 1).toCharArray()[0]);
            char b = (char)this.config.hotkey().getKeyCode();
            typedText.substring(typedText.length() - 1);
            if (a == b) {
                this.client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, subTypedText);
            }
        }
    }

    @Subscribe
    private void onBeforeRender(BeforeRender event) {
        Widget chatbox = this.client.getWidget(WidgetInfo.CHATBOX_INPUT);
        if (chatbox != null && !chatbox.isHidden()) {
            if (!this.tradeActive && this.client.getVarcIntValue(41) == 6) {
                this.lastTypingState = this.typing;
                this.typing = true;
                this.tradeActive = true;
            } else if (this.tradeActive && this.client.getVarcIntValue(41) != 6) {
                this.typing = this.lastTypingState;
                this.tradeActive = false;
            }
            if (this.typing) {
                if (!chatbox.getText().startsWith("[SOCKET CHAT] ")) {
                    chatbox.setText("[SOCKET CHAT] " + chatbox.getText());
                }
            } else if (chatbox.getText().startsWith("[SOCKET CHAT] ")) {
                chatbox.setText(chatbox.getText().substring(13));
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (this.config.hotkey().matches(e)) {
            this.typing = !this.typing;
            this.clientThread.invokeLater(() -> {
                try {
                    this.removeHotkey();
                }
                catch (InterruptedException var2) {
                    var2.printStackTrace();
                }
            });
        }
        if (e.getKeyCode() == 10) {
            String typedText = this.client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
            if (this.typing) {
                if (typedText.startsWith("/")) {
                    if (!this.config.overrideSlash()) {
                        this.sendMessage(typedText);
                        this.client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
                    }
                } else {
                    this.sendMessage(typedText);
                    this.client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
                }
            }
        }
    }

    private void sendMessage(String msg) {
        if (!msg.equals("")) {
            JSONArray data = new JSONArray();
            JSONObject jsonmsg = new JSONObject();
            jsonmsg.put("msg", " " + msg);
            if (this.config.getIcon() != 0) {
                jsonmsg.put("sender", "<img=" + this.config.getIcon() + ">" + this.client.getLocalPlayer().getName());
            } else {
                jsonmsg.put("sender", this.client.getLocalPlayer().getName());
            }
            data.put(jsonmsg);
            JSONObject send = new JSONObject();
            send.put("sChat", data);
            this.eventBus.post(new SocketBroadcastPacket(send));
            if (this.config.singleText()) {
                this.typing = false;
            }
        }
    }

    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        try {
            JSONObject payload = event.getPayload();
            if (!payload.has("sChat")) {
                return;
            }
            Date date = new Date();
            JSONArray data = payload.getJSONArray("sChat");
            JSONObject jsonmsg = data.getJSONObject(0);
            String sender = jsonmsg.getString("sender");
            String msg = jsonmsg.getString("msg");
            ChatMessageType cmt = this.config.overrideTradeButton() ? ChatMessageType.TRADE : ChatMessageType.GAMEMESSAGE;
            String dateTime = "";
            if (this.config.getDateStamp()) {
                dateTime = this.config.getFreedomUnits() ? dateTime + this.formatterrr.format(date) : dateTime + this.formatter.format(date);
            }
            if (this.config.getTimeStamp()) {
                dateTime = !dateTime.equals("") ? dateTime + " | " + this.formatterr.format(date) : dateTime + this.formatterr.format(date);
            }
            String dateTimeString = "[" + dateTime + "] ";
            String customMsg = "";
            if (!this.config.showSomeStupidShit().equals("")) {
                customMsg = "[" + this.config.showSomeStupidShit() + "] ";
            }
            if (!dateTime.equals("")) {
                this.client.addChatMessage(cmt, "", ColorUtil.prependColorTag(dateTimeString, this.config.getDateTimeColor()) + customMsg + ColorUtil.prependColorTag(sender, this.config.getNameColor()) + ":" + ColorUtil.prependColorTag(msg, this.config.messageColor()), null, false);
            } else {
                this.client.addChatMessage(cmt, "", customMsg + ColorUtil.prependColorTag(sender, this.config.getNameColor()) + ":" + ColorUtil.prependColorTag(msg, this.config.messageColor()), null, false);
            }
        }
        catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}

