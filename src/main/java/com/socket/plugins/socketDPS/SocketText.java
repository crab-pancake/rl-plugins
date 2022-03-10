/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.base.Splitter
 *  org.apache.commons.lang3.StringUtils
 *  org.apache.commons.text.WordUtils
 *  org.apache.commons.text.similarity.JaroWinklerDistance
 */
package com.socket.plugins.socketDPS;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;

public class SocketText {
    private static final StringBuilder SB = new StringBuilder(64);
    private static final Pattern TAG_REGEXP = Pattern.compile("<[^>]*>");
    public static final JaroWinklerDistance DISTANCE = new JaroWinklerDistance();
    public static final Splitter COMMA_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();

    public static List<String> fromCSV(String input) {
        return COMMA_SPLITTER.splitToList(input);
    }

    public static String toCSV(Collection<String> input) {
        return COMMA_JOINER.join(input);
    }

    public static String removeTags(String str, boolean removeLevels) {
        int close;
        int open;
        int levelIdx;
        int strLen = str.length();
        if (removeLevels && (levelIdx = StringUtils.lastIndexOf(str, "  (level")) >= 0) {
            strLen = levelIdx;
        }
        if ((open = StringUtils.indexOf(str, 60)) == -1 || (close = StringUtils.indexOf(str, 62, open)) == -1) {
            return strLen == str.length() ? str : str.substring(0, strLen - 1);
        }
        if (open == 0) {
            open = close + 1;
            if (open >= strLen) {
                return "";
            }
            if ((open = StringUtils.indexOf(str, 60, open)) == -1 || StringUtils.indexOf(str, 62, open) == -1) {
                return StringUtils.substring(str, close + 1);
            }
            open = 0;
        }
        SB.setLength(0);
        int i = 0;
        while (true) {
            if (open != i) {
                SB.append(str.charAt(i++));
                continue;
            }
            i = close + 1;
            open = StringUtils.indexOf(str, 60, close);
            if (open == -1 || (close = StringUtils.indexOf(str, 62, open)) == -1 || i >= strLen) break;
        }
        while (i < strLen) {
            SB.append(str.charAt(i++));
        }
        return SB.toString();
    }

    public static String removeTags(String str) {
        return SocketText.removeTags(str, false);
    }

    public static String standardize(String str, boolean removeLevel) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        return SocketText.removeTags(str, removeLevel).replace(' ', ' ').trim().toLowerCase();
    }

    public static String standardize(String str) {
        return SocketText.standardize(str, false);
    }

    public static String toJagexName(String str) {
        char[] chars = str.toCharArray();
        int newIdx = 0;
        int strLen = str.length();
        for(int oldIdx = 0; oldIdx < strLen; ++oldIdx) {
            char c = chars[oldIdx];
            if (c == '-' || c == '_' || c == ' ') {
                if (oldIdx == strLen - 1 || newIdx == 0 || chars[newIdx - 1] == ' ') {
                    continue;
                }

                c = ' ';
            }

            if (c <= 127) {
                chars[newIdx++] = c;
            }
        }

        return new String(chars, 0, newIdx);
    }


    public static String sanitizeMultilineText(String str) {
        return SocketText.removeTags(str.replaceAll("-<br>", "-").replaceAll("<br>", " ").replaceAll("[ ]+", " "));
    }

    public static String escapeJagex(String str) {
        StringBuilder out = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == '<') {
                out.append("<lt>");
                continue;
            }
            if (c == '>') {
                out.append("<gt>");
                continue;
            }
            if (c == '\n') {
                out.append("<br>");
                continue;
            }
            if (c == '\r') continue;
            out.append(c);
        }
        return out.toString();
    }

    public static String sanitize(String name) {
        String cleaned = name.contains("<img") ? name.substring(name.lastIndexOf(62) + 1) : name;
        return cleaned.replace(' ', ' ');
    }

    public static String titleCase(Enum o) {
        String toString = o.toString();
        if (o.name().equals(toString)) {
            return WordUtils.capitalize(toString.toLowerCase(), new char[]{'_'}).replace('_', ' ');
        }
        return toString;
    }

    public static boolean matchesSearchTerms(String[] searchTerms, Collection<String> keywords) {
        for (String term : searchTerms) {
            if (keywords.stream().anyMatch(t -> t.contains(term) || DISTANCE.apply(t, term) > 0.9)) continue;
            return false;
        }
        return true;
    }
}

