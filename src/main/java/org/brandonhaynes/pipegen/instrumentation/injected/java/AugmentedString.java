package org.brandonhaynes.pipegen.instrumentation.injected.java;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.nio.charset.Charset;
import java.util.*;

public class AugmentedString extends org.brandonhaynes.pipegen.instrumentation.injected.java.String {
//public class AugmentedString extends org.brandonhaynes.pipegen.instrumentation.injected.java.String {
    private String decoratedString = null;
    private Object[] state;

    public static AugmentedString decorate(Object o) {
        return new AugmentedString(o);
    }

    public AugmentedString() {
        this.state = new Object[0];
    }

    public AugmentedString(String var1) {
        decoratedString = var1;
    }

    public AugmentedString(Object o) {
        this.state = new Object[1];
        this.state[0] = o;
    }

    public AugmentedString(Object[] state) {
        this.state = state;
    }

    private String collapse() {
        return decoratedString == null
                ? (decoratedString = Joiner.on("").join(state))
                : decoratedString;
    }

    private boolean hasCollapsed() { return decoratedString != null; }

    @Override
    public int length() {
        return collapse().length();
    }

    @Override
    public boolean isEmpty() {
        return state.length == 0;
    }

    @Override
    public char charAt(int var1) {
        return collapse().charAt(var1);
    }

    @Override
    public int codePointAt(int var1) {
        return collapse().codePointAt(var1);
    }

    @Override
    public int codePointBefore(int var1) {
        return collapse().codePointBefore(var1);
    }

    @Override
    public int codePointCount(int var1, int var2) {
        return collapse().codePointCount(var1, var2);
    }

    @Override
    public int offsetByCodePoints(int var1, int var2) {
        return collapse().offsetByCodePoints(var1, var2);
    }

    @Override
    public void getChars(int var1, int var2, char[] var3, int var4) {
        collapse().getChars(var1, var2, var3, var4);
    }

    @Override
    @Deprecated
    public void getBytes(int var1, int var2, byte[] var3, int var4) {
        collapse().getBytes(var1, var2, var3, var4);
    }

    @Override
    public byte[] getBytes(String var1) throws UnsupportedEncodingException {
        return collapse().getBytes(var1);
    }

    @Override
    public byte[] getBytes(Charset var1) {
        return collapse().getBytes(var1);
    }

    @Override
    public byte[] getBytes() {
        return collapse().getBytes();
    }

    @Override
    public boolean equals(Object var1) {
        return collapse().equals(var1);
    }

    @Override
    public boolean contentEquals(StringBuffer var1) {
        return collapse().contentEquals(var1);
    }

    @Override
    public boolean contentEquals(CharSequence var1) {
        return collapse().contentEquals(var1);
    }

    @Override
    public boolean equalsIgnoreCase(String var1) {
        return collapse().equalsIgnoreCase(var1);
    }

    @Override
    public int compareTo(String var1) {
        return collapse().compareTo(var1);
    }

    @Override
    public int compareToIgnoreCase(String var1) {
        return collapse().compareToIgnoreCase(var1);
    }

    @Override
    public boolean regionMatches(int var1, String var2, int var3, int var4) {
        return collapse().regionMatches(var1, var2, var3, var4);
    }

    @Override
    public boolean regionMatches(boolean var1, int var2, String var3, int var4, int var5) {
        return collapse().regionMatches(var1, var2, var3, var4, var5);
    }

    @Override
    public boolean startsWith(String var1, int var2) {
        return collapse().startsWith(var1, var2);
    }

    @Override
    public boolean startsWith(String var1) {
        return collapse().startsWith(var1);
    }

    @Override
    public boolean endsWith(String var1) {
        return collapse().endsWith(var1);
    }

    @Override
    public int hashCode() {
        return collapse().hashCode();
    }

    @Override
    public int indexOf(int var1) {
        return collapse().indexOf(var1);
    }

    @Override
    public int indexOf(int var1, int var2) {
        return collapse().indexOf(var1, var2);
    }

    @Override
    public int lastIndexOf(int var1) {
        return collapse().lastIndexOf(var1);
    }

    @Override
    public int lastIndexOf(int var1, int var2) {
        return collapse().lastIndexOf(var1, var2);
    }

    @Override
    public int indexOf(String var1) {
        return collapse().indexOf(var1);
    }

    @Override
    public int indexOf(String var1, int var2) {
        return collapse().indexOf(var1, var2);
    }

    @Override
    public int lastIndexOf(String var1) {
        return collapse().lastIndexOf(var1);
    }

    @Override
    public int lastIndexOf(String var1, int var2) {
        return collapse().lastIndexOf(var1, var2);
    }

    @Override
    public String substring(int var1) {
        return collapse().substring(var1);
    }

    @Override
    public String substring(int var1, int var2) {
        return collapse().substring(var1, var2);
    }

    @Override
    public CharSequence subSequence(int var1, int var2) {
        return collapse().subSequence(var1, var2);
    }

    @Override
    public String concat(String var1) {
        return collapse().concat(var1);
    }

    @Override
    public String replace(char var1, char var2) {
        return collapse().replace(var1, var2);
    }

    @Override
    public boolean matches(String var1) {
        return collapse().matches(var1);
    }

    @Override
    public boolean contains(CharSequence var1) {
        return collapse().contains(var1);
    }

    @Override
    public String replaceFirst(String var1, String var2) {
        return collapse().replaceFirst(var1, var2);
    }

    @Override
    public String replaceAll(String var1, String var2) {
        return collapse().replaceAll(var1, var2);
    }

    @Override
    public String replace(CharSequence var1, CharSequence var2) {
        return collapse().replace(var1, var2);
    }

    @Override
    public String[] split(String var1, int var2) {
        return collapse().split(var1, var2);
    }

    @Override
    public String[] split(String var1) {
        return collapse().split(var1);
    }

    @Override
    public String toLowerCase(Locale var1) {
        return collapse().toLowerCase();
    }

    @Override
    public String toLowerCase() {
        return collapse().toLowerCase();
    }

    @Override
    public String toUpperCase(Locale var1) {
        return collapse().toUpperCase(var1);
    }

    @Override
    public String toUpperCase() {
        return collapse().toUpperCase();
    }

    @Override
    public String trim() {
        return collapse().trim();
    }

    @Override
    public String toString() {
        return (String)(Object)this;
    }

    @Override
    public char[] toCharArray() {
        return collapse().toCharArray();
    }
}
