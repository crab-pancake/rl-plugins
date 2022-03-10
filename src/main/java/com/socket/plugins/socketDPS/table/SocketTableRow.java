/*
 * Decompiled with CFR 0.150.
 */
package com.socket.plugins.socketDPS.table;

import java.awt.Color;
import java.util.List;

public class SocketTableRow {
    Color rowColor;
    SocketTableAlignment rowAlignment;
    List<SocketTableElement> elements;

    public void setRowColor(Color rowColor) {
        this.rowColor = rowColor;
    }

    public void setRowAlignment(SocketTableAlignment rowAlignment) {
        this.rowAlignment = rowAlignment;
    }

    public void setElements(List<SocketTableElement> elements) {
        this.elements = elements;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SocketTableRow)) {
            return false;
        }
        SocketTableRow other = (SocketTableRow)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Color this$rowColor = this.getRowColor();
        Color other$rowColor = other.getRowColor();
        if (this$rowColor == null ? other$rowColor != null : !((Object)this$rowColor).equals(other$rowColor)) {
            return false;
        }
        SocketTableAlignment this$rowAlignment = this.getRowAlignment();
        SocketTableAlignment other$rowAlignment = other.getRowAlignment();
        if (this$rowAlignment == null ? other$rowAlignment != null : !((Object) this$rowAlignment).equals(other$rowAlignment)) {
            return false;
        }
        List<SocketTableElement> this$elements = this.getElements();
        List<SocketTableElement> other$elements = other.getElements();
        return !(this$elements == null ? other$elements != null : !((Object)this$elements).equals(other$elements));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SocketTableRow;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Color $rowColor = this.getRowColor();
        result = result * 59 + ($rowColor == null ? 43 : ((Object)$rowColor).hashCode());
        SocketTableAlignment $rowAlignment = this.getRowAlignment();
        result = result * 59 + ($rowAlignment == null ? 43 : ((Object) $rowAlignment).hashCode());
        List<SocketTableElement> $elements = this.getElements();
        return result * 59 + ($elements == null ? 43 : ((Object)$elements).hashCode());
    }

    public String toString() {
        return "SocketTableRow(rowColor=" + this.getRowColor() + ", rowAlignment=" + this.getRowAlignment() + ", elements=" + this.getElements() + ")";
    }

    SocketTableRow(Color rowColor, SocketTableAlignment rowAlignment, List<SocketTableElement> elements) {
        this.rowColor = rowColor;
        this.rowAlignment = rowAlignment;
        this.elements = elements;
    }

    public static SocketTableRowBuilder builder() {
        return new SocketTableRowBuilder();
    }

    public Color getRowColor() {
        return this.rowColor;
    }

    public SocketTableAlignment getRowAlignment() {
        return this.rowAlignment;
    }

    public List<SocketTableElement> getElements() {
        return this.elements;
    }

    public static class SocketTableRowBuilder {
        private Color rowColor;
        private SocketTableAlignment rowAlignment;
        private List<SocketTableElement> elements;

        public SocketTableRowBuilder rowColor(Color rowColor) {
            this.rowColor = rowColor;
            return this;
        }

        public SocketTableRowBuilder rowAlignment(SocketTableAlignment rowAlignment) {
            this.rowAlignment = rowAlignment;
            return this;
        }

        public SocketTableRowBuilder elements(List<SocketTableElement> elements) {
            this.elements = elements;
            return this;
        }

        public SocketTableRow build() {
            return new SocketTableRow(this.rowColor, this.rowAlignment, this.elements);
        }

        public String toString() {
            return "SocketTableRow.SocketTableRowBuilder(rowColor=" + this.rowColor + ", rowAlignment=" + this.rowAlignment + ", elements=" + this.elements + ")";
        }
    }
}

