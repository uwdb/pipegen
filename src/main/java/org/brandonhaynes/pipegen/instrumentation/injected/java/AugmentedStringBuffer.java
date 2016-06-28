package org.brandonhaynes.pipegen.instrumentation.injected.java;

public class AugmentedStringBuffer extends StringBuffer {
    private final AugmentedStringBuilder builder;

    public AugmentedStringBuffer() {
        super();
        builder = new AugmentedStringBuilder();
    }

    public AugmentedStringBuffer(AugmentedString var1) {
        super();
        builder = new AugmentedStringBuilder(var1);
    }

    public AugmentedStringBuffer(int var1) {
        super(var1);
        builder = new AugmentedStringBuilder();
    }

    public AugmentedStringBuffer(java.lang.String var1) {
        super();
        builder = new AugmentedStringBuilder(var1);
    }

    public AugmentedStringBuffer(CharSequence var1) {
        super();
        builder = new AugmentedStringBuilder(var1);
    }

    public synchronized StringBuffer append(AugmentedString var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized int length() {
        return builder.length();
    }

    @Override
    public synchronized int capacity() {
        return builder.capacity();
    }

    @Override
    public synchronized void ensureCapacity(int var1) {
    }

    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }

    @Override
    public synchronized void setLength(int var1) {
        builder.setLength(var1);
    }

    @Override
    public synchronized char charAt(int var1) {
        return builder.charAt(var1);
    }

    @Override
    public synchronized int codePointAt(int var1) {
        return builder.codePointAt(var1);
    }

    @Override
    public synchronized int codePointBefore(int var1) {
        return builder.codePointBefore(var1);
    }

    @Override
    public synchronized int codePointCount(int var1, int var2) {
        return builder.codePointCount(var1, var2);
    }

    @Override
    public synchronized int offsetByCodePoints(int var1, int var2) {
        return builder.offsetByCodePoints(var1, var2);
    }

    @Override
    public synchronized void getChars(int var1, int var2, char[] var3, int var4) {
        builder.getChars(var1, var2, var3, var4);
    }

    @Override
    public synchronized void setCharAt(int var1, char var2) {
        builder.setCharAt(var1, var2);
    }

    @Override
    public synchronized StringBuffer append(Object var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(java.lang.String var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(CharSequence var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(CharSequence var1, int var2, int var3) {
        builder.append(var1, var2, var3);
        return this;
    }

    @Override
    public synchronized StringBuffer append(char[] var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(char[] var1, int var2, int var3) {
        builder.append(var1, var2, var3);
        return this;
    }

    @Override
    public synchronized StringBuffer append(boolean var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(char var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(int var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer appendCodePoint(int var1) {
        builder.appendCodePoint(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(long var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(float var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer append(double var1) {
        builder.append(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer delete(int var1, int var2) {
        builder.delete(var1, var2);
        return this;
    }

    @Override
    public synchronized StringBuffer deleteCharAt(int var1) {
        builder.deleteCharAt(var1);
        return this;
    }

    @Override
    public synchronized StringBuffer replace(int var1, int var2, java.lang.String var3) {
        builder.replace(var1, var2, var3);
        return this;
    }

    @Override
    public synchronized java.lang.String substring(int var1) {
        return builder.substring(var1);
    }

    @Override
    public synchronized CharSequence subSequence(int var1, int var2) {
        return builder.substring(var1, var2);
    }

    @Override
    public synchronized java.lang.String substring(int var1, int var2) {
        return builder.substring(var1, var2);
    }

    @Override
    public synchronized StringBuffer insert(int var1, char[] var2, int var3, int var4) {
        builder.insert(var1, var2, var3, var4);
        return this;
    }

    @Override
    public synchronized StringBuffer insert(int var1, Object var2) {
        builder.insert(var1, java.lang.String.valueOf(var2));
        return this;
    }

    @Override
    public synchronized StringBuffer insert(int var1, java.lang.String var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public synchronized StringBuffer insert(int var1, char[] var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public StringBuffer insert(int var1, CharSequence var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public synchronized StringBuffer insert(int var1, CharSequence var2, int var3, int var4) {
        builder.insert(var1, var2, var3, var4);
        return this;
    }

    @Override
    public StringBuffer insert(int var1, boolean var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public synchronized StringBuffer insert(int var1, char var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public StringBuffer insert(int var1, int var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public StringBuffer insert(int var1, long var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public StringBuffer insert(int var1, float var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public StringBuffer insert(int var1, double var2) {
        builder.insert(var1, var2);
        return this;
    }

    @Override
    public int indexOf(java.lang.String var1) {
        return builder.indexOf(var1);
    }

    @Override
    public synchronized int indexOf(java.lang.String var1, int var2) {
        return builder.indexOf(var1, var2);
    }

    @Override
    public int lastIndexOf(java.lang.String var1) {
        return builder.lastIndexOf(var1);
    }

    @Override
    public synchronized int lastIndexOf(java.lang.String var1, int var2) {
        return builder.lastIndexOf(var1, var2);
    }

    @Override
    public synchronized StringBuffer reverse() {
        builder.reverse();
        return this;
    }

    @Override
    public synchronized java.lang.String toString() {
        return builder.toString();
    }
}
