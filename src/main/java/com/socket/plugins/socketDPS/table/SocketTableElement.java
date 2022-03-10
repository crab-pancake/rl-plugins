/*
 * Decompiled with CFR 0.150.
 */
package com.socket.plugins.socketDPS.table;

import java.awt.Color;
import java.util.Objects;

public class SocketTableElement {
    SocketTableAlignment alignment;
    Color color;
    String content;

    public void setAlignment(SocketTableAlignment alignment) {
        this.alignment = alignment;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SocketTableElement)) {
            return false;
        }
        SocketTableElement other = (SocketTableElement)o;
        if (!other.canEqual(this)) {
            return false;
        }
        SocketTableAlignment this$alignment = this.getAlignment();
        SocketTableAlignment other$alignment = other.getAlignment();
        if (this$alignment == null ? other$alignment != null : !((Object) this$alignment).equals(other$alignment)) {
            return false;
        }
        Color this$color = this.getColor();
        Color other$color = other.getColor();
        if (this$color == null ? other$color != null : !((Object)this$color).equals(other$color)) {
            return false;
        }
        String this$content = this.getContent();
        String other$content = other.getContent();
        return Objects.equals(this$content, other$content);
    }

    protected boolean canEqual(Object other) {
        return other instanceof SocketTableElement;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        SocketTableAlignment $alignment = this.getAlignment();
        result = result * 59 + ($alignment == null ? 43 : ((Object) $alignment).hashCode());
        Color $color = this.getColor();
        result = result * 59 + ($color == null ? 43 : ((Object)$color).hashCode());
        String $content = this.getContent();
        return result * 59 + ($content == null ? 43 : $content.hashCode());
    }

    public String toString() {
        return "SocketTableElement(alignment=" + this.getAlignment() + ", color=" + this.getColor() + ", content=" + this.getContent() + ")";
    }

    SocketTableElement(SocketTableAlignment alignment, Color color, String content) {
        this.alignment = alignment;
        this.color = color;
        this.content = content;
    }

    public static SocketTableElementBuilder builder() {
        return new SocketTableElementBuilder();
    }

    public SocketTableAlignment getAlignment() {
        return this.alignment;
    }

    public Color getColor() {
        return this.color;
    }

    public String getContent() {
        return this.content;
    }

    public static class SocketTableElementBuilder {
        private SocketTableAlignment alignment;
        private Color color;
        private String content;

        public SocketTableElementBuilder alignment(SocketTableAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public SocketTableElementBuilder color(Color color) {
            this.color = color;
            return this;
        }

        public SocketTableElementBuilder content(String content) {
            this.content = content;
            return this;
        }

        public SocketTableElement build() {
            return new SocketTableElement(this.alignment, this.color, this.content);
        }

        public String toString() {
            return "SocketTableElement.SocketTableElementBuilder(alignment=" + this.alignment + ", color=" + this.color + ", content=" + this.content + ")";
        }
    }
}

