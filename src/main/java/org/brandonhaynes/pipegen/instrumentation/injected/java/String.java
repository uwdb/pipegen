package org.brandonhaynes.pipegen.instrumentation.injected.java;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class String implements Serializable, Comparable<java.lang.String>, CharSequence {
    private static java.lang.String collapse() { return ""; }

    public int length() { return collapse().length(); }

    public boolean isEmpty() {
        return true;
    }

    public char charAt(int var1) {
        return collapse().charAt(var1);
    }

    public int codePointAt(int var1) {
        return collapse().codePointAt(var1);
    }

    public int codePointBefore(int var1) {
        return collapse().codePointBefore(var1);
    }

    public int codePointCount(int var1, int var2) {
        return collapse().codePointCount(var1, var2);
    }

    public int offsetByCodePoints(int var1, int var2) {
        return collapse().offsetByCodePoints(var1, var2);
    }

    public void getChars(int var1, int var2, char[] var3, int var4) {
        collapse().getChars(var1, var2, var3, var4);
    }

    @Deprecated
    public void getBytes(int var1, int var2, byte[] var3, int var4) {
        collapse().getBytes(var1, var2, var3, var4);
    }

    public byte[] getBytes(java.lang.String var1) throws UnsupportedEncodingException {
        return collapse().getBytes(var1);
    }

    public byte[] getBytes(Charset var1) {
        return collapse().getBytes(var1);
    }

    public byte[] getBytes() {
        return collapse().getBytes();
    }

    public boolean equals(Object var1) {
        return collapse().equals(var1);
    }

    public boolean contentEquals(StringBuffer var1) {
        return collapse().contentEquals(var1);
    }

    public boolean contentEquals(CharSequence var1) {
        return collapse().contentEquals(var1);
    }

    public boolean equalsIgnoreCase(java.lang.String var1) {
        return collapse().equalsIgnoreCase(var1);
    }

    public int compareTo(java.lang.String var1) {
        return collapse().compareTo(var1);
    }

    public int compareToIgnoreCase(java.lang.String var1) {
        return collapse().compareToIgnoreCase(var1);
    }

    public boolean regionMatches(int var1, java.lang.String var2, int var3, int var4) {
        return collapse().regionMatches(var1, var2, var3, var4);
    }

    public boolean regionMatches(boolean var1, int var2, java.lang.String var3, int var4, int var5) {
        return collapse().regionMatches(var1, var2, var3, var4, var5);
    }

    public boolean startsWith(java.lang.String var1, int var2) {
        return collapse().startsWith(var1, var2);
    }

    public boolean startsWith(java.lang.String var1) {
        return collapse().startsWith(var1);
    }

    public boolean endsWith(java.lang.String var1) {
        return collapse().endsWith(var1);
    }

    public int hashCode() {
        return collapse().hashCode();
    }

    public int indexOf(int var1) {
        return collapse().indexOf(var1);
    }

    public int indexOf(int var1, int var2) {
        return collapse().indexOf(var1, var2);
    }

    public int lastIndexOf(int var1) {
        return collapse().lastIndexOf(var1);
    }

    public int lastIndexOf(int var1, int var2) {
        return collapse().lastIndexOf(var1, var2);
    }

    public int indexOf(java.lang.String var1) {
        return collapse().indexOf(var1);
    }

    public int indexOf(java.lang.String var1, int var2) {
        return collapse().indexOf(var1, var2);
    }

    public int lastIndexOf(java.lang.String var1) {
        return collapse().lastIndexOf(var1);
    }

    public int lastIndexOf(java.lang.String var1, int var2) {
        return collapse().lastIndexOf(var1, var2);
    }

    public java.lang.String substring(int var1) {
        return collapse().substring(var1);
    }

    public java.lang.String substring(int var1, int var2) {
        return collapse().substring(var1, var2);
    }

    public CharSequence subSequence(int var1, int var2) {
        return collapse().subSequence(var1, var2);
    }

    public java.lang.String concat(java.lang.String var1) {
        return collapse().concat(var1);
    }

    public java.lang.String replace(char var1, char var2) {
        return collapse().replace(var1, var2);
    }

    public boolean matches(java.lang.String var1) {
        return collapse().matches(var1);
    }

    public boolean contains(CharSequence var1) {
        return collapse().contains(var1);
    }

    public java.lang.String replaceFirst(java.lang.String var1, java.lang.String var2) {
        return collapse().replaceFirst(var1, var2);
    }

    public java.lang.String replaceAll(java.lang.String var1, java.lang.String var2) {
        return collapse().replaceAll(var1, var2);
    }

    public java.lang.String replace(CharSequence var1, CharSequence var2) {
        return collapse().replace(var1, var2);
    }

    public java.lang.String[] split(java.lang.String var1, int var2) {
        return collapse().split(var1, var2);
    }

    public java.lang.String[] split(java.lang.String var1) {
        return collapse().split(var1);
    }

    public java.lang.String toLowerCase(Locale var1) {
        return collapse().toLowerCase();
    }

    public java.lang.String toLowerCase() {
        return collapse().toLowerCase();
    }

    public java.lang.String toUpperCase(Locale var1) {
        return collapse().toUpperCase(var1);
    }

    public java.lang.String toUpperCase() {
        return collapse().toUpperCase();
    }

    public java.lang.String trim() {
        return collapse().trim();
    }

    public java.lang.String toString() {
        return (java.lang.String)(Object)this;
    }

    public char[] toCharArray() {
        return collapse().toCharArray();
    }
}
