package org.brandonhaynes.pipegen.instrumentation.injected.java;

import java.util.Arrays;

public class AugmentedStringBuilder extends org.brandonhaynes.pipegen.instrumentation.injected.java.StringBuilder {
    private AugmentedString state;

    public static AugmentedStringBuilder decorate() {
        return new AugmentedStringBuilder();
    }

    public AugmentedStringBuilder() {
        this(16);
    }

    public AugmentedStringBuilder(int capacity) {
        super(capacity);
        state = new AugmentedString();
    }

    public AugmentedStringBuilder(AugmentedString s) {
        super();
        state = s;
    }

    public AugmentedStringBuilder(java.lang.String s) {
        super();
        state = AugmentedString.decorate(s);
    }

    public AugmentedStringBuilder(CharSequence s) {
        super();
        state = AugmentedString.decorate(s);
    }

    @Override
    public int length() {
        return state.length();
    }

    public StringBuilder append(AugmentedString a) {
        state = AugmentedString.concat(state, a);
        return this;
    }

    @Override
    public StringBuilder append(Object o) {
        state = AugmentedString.concat(state, o);
        return this;
    }

    @Override
    public StringBuilder append(java.lang.String var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(StringBuffer var1) {
        state = AugmentedString.concat(state, var1.toString());
        return this;
    }

    @Override
    public StringBuilder append(CharSequence var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(CharSequence var1, int var2, int var3) {
        state = AugmentedString.concat(state, var1.subSequence(var2, var3));
        return this;
    }

    @Override
    public StringBuilder append(char[] var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(char[] var1, int var2, int var3) {
        state = AugmentedString.concat(state, Arrays.copyOfRange(var1, var2, var3));
        return this;
    }

    @Override
    public StringBuilder append(boolean var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(char var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(int var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(long var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(float var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder append(double var1) {
        state = AugmentedString.concat(state, var1);
        return this;
    }

    @Override
    public StringBuilder appendCodePoint(int var1) {
        state = AugmentedString.concat(state, Character.toChars(var1));
        return this;
    }

    @Override
    public StringBuilder delete(int var1, int var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder deleteCharAt(int var1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder replace(int var1, int var2, java.lang.String var3) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, char[] var2, int var3, int var4) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, Object var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, java.lang.String var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, char[] var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, CharSequence var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, CharSequence var2, int var3, int var4) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, boolean var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, char var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, int var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, long var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, float var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuilder insert(int var1, double var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(java.lang.String var1) {
        return state.indexOf(var1);
    }

    @Override
    public int indexOf(java.lang.String var1, int var2) {
        return state.indexOf(var1, var2);
    }

    @Override
    public int lastIndexOf(java.lang.String var1) {
        return state.lastIndexOf(var1);
    }

    @Override
    public int lastIndexOf(java.lang.String var1, int var2) {
        return state.lastIndexOf(var1, var2);
    }

    @Override
    public StringBuilder reverse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.lang.String toString() {
        return state;
    }
}
