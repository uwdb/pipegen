package org.brandonhaynes.pipegen.instrumentation.injected.java;

import org.brandonhaynes.pipegen.utilities.ArrayUtilities;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.nio.charset.Charset;
import java.util.Locale;

public class AugmentedString extends org.brandonhaynes.pipegen.instrumentation.injected.java.String {
    private String decoratedString = null;
    private Object[] state;

    private static final Object[] emptyObjectArray = new Object[0];
    public static final AugmentedString empty = new AugmentedString();
    public static final AugmentedString newline = new AugmentedString('\n');

    public static AugmentedString decorate(short i) {
        return new AugmentedString(i);
    }
    public static AugmentedString decorate(int i) {
        return new AugmentedString(i);
    }
    public static AugmentedString decorate(long l) {
        return new AugmentedString(l);
    }
    public static AugmentedString decorate(byte b) {
        return new AugmentedString(b);
    }
    public static AugmentedString decorate(float f) {
        return new AugmentedString(f);
    }
    public static AugmentedString decorate(double d) {
        return new AugmentedString(d);
    }
    static AugmentedString decorate(Object o) { return new AugmentedString(o); }

    public static AugmentedString concat(AugmentedString left, Object right) {
        if(right instanceof AugmentedString)
            return concat(left, (AugmentedString)right);
        else
            return new AugmentedString(ArrayUtilities.concat(left.state, right));
    }

    public static AugmentedString concat(AugmentedString left, AugmentedString right) {
        return new AugmentedString(ArrayUtilities.concat(left.state, right.state));
    }

    public static AugmentedString separate(AugmentedString value, char delimiter, char suffix) {
        Object[] state = new Object[value.state.length * 2];
        for(int i = 0; i < value.state.length; i++) {
            state[2 * i] = value.state[i];
            state[2 * i + 1] = i + 1 < value.state.length ? delimiter : suffix;
        }
        return new AugmentedString(state);
    }

    public AugmentedString() {
        this.state = emptyObjectArray;
    }

    public AugmentedString(String s) {
        this((Object)s);
        decoratedString = s instanceof AugmentedString ? ((AugmentedString)s).decoratedString : s;
    }

    public AugmentedString(Object o) {
        this.state = o instanceof AugmentedString ? ((AugmentedString)o).state : new Object[] {o};
    }

    public AugmentedString(Object... state) {
        this.state = state;
    }

    private String collapse() {
        if(!hasCollapsed()) {
            StringBuilder builder = new StringBuilder();
            for (Object aState : state) builder.append(aState);
            decoratedString = builder.toString();
        }

        return decoratedString;
    }

    private boolean hasCollapsed() { return decoratedString != null; }
    public Object[] getState() { return state; }

    public boolean isDelimiter(int index) { return state[index].equals(",") || state[index].equals(','); }
    public boolean isNewline(int index) { return state[index].equals("\n") || state[index].equals('\n'); }
    public boolean isLong(int index) { return state[index] instanceof Long; }
    public boolean isInteger(int index) { return state[index] instanceof Integer; }
    public boolean isByte(int index) { return state[index] instanceof Byte; }
    public boolean isFloat(int index) { return state[index] instanceof Float; }
    public boolean isDouble(int index) { return state[index] instanceof Double; }

    public long getLong(int index) { return (long)state[index]; }
    public int getInteger(int index) { return (int)state[index]; }
    public byte getByte(int index) { return (byte)state[index]; }
    public float getFloat(int index) { return (float)state[index]; }
    public double getDouble(int index) { return (double)state[index]; }


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
    @SuppressWarnings("deprecation")
    public void getBytes(int var1, int var2, byte[] var3, int var4) {
        collapse().getBytes(var1, var2, var3, var4);
    }

    @Override
    @Nonnull
    public byte[] getBytes(String var1) throws UnsupportedEncodingException {
        return collapse().getBytes(var1);
    }

    @Override
    @Nonnull
    public byte[] getBytes(Charset var1) {
        return collapse().getBytes(var1);
    }

    @Override
    @Nonnull
    public byte[] getBytes() {
        return collapse().getBytes();
    }

    @Override
    public boolean equals(Object var1) {
        return var1 instanceof String && collapse().equals(var1);
    }

    @Override
    public boolean contentEquals(@Nonnull java.lang.StringBuffer var1) {
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
    @Nonnull
    public String substring(int var1) {
        return collapse().substring(var1);
    }

    @Override
    @Nonnull
    public String substring(int var1, int var2) {
        return collapse().substring(var1, var2);
    }

    @Override
    @Nonnull
    public CharSequence subSequence(int var1, int var2) {
        return collapse().subSequence(var1, var2);
    }

    @Override
    @Nonnull
    public String concat(String var1) {
        return collapse().concat(var1);
    }

    @Override
    @Nonnull
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

    public boolean containsNonNumeric(CharSequence c) {
        for (Object v : state)
            if ((v instanceof AugmentedString && ((AugmentedString) v).containsNonNumeric(c)) ||
                    (v instanceof String && ((String) v).contains(c)) ||
                    (v instanceof Character && c.length() == 1 && v.equals(c.charAt(0))))
                return true;
        return false;
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
    @Nonnull
    public String[] split(String var1, int var2) {
        return collapse().split(var1, var2);
    }

    @Override
    @Nonnull
    public String[] split(String var1) {
        return collapse().split(var1);
    }

    @Override
    @Nonnull
    public String toLowerCase(Locale var1) {
        return collapse().toLowerCase();
    }

    @Override
    @Nonnull
    public String toLowerCase() {
        return collapse().toLowerCase();
    }

    @Override
    @Nonnull
    public String toUpperCase(Locale var1) {
        return collapse().toUpperCase(var1);
    }

    @Override
    @Nonnull
    public String toUpperCase() {
        return collapse().toUpperCase();
    }

    @Override
    @Nonnull
    public String trim() {
        return collapse().trim();
    }

    @Override
    @Nonnull
    public String toString() {
        return this;
    }

    @Override
    @Nonnull
    public char[] toCharArray() {
        return collapse().toCharArray();
    }

    public void printState(PrintStream stream) {
        stream.println(hasCollapsed() ? "[Collapsed]" : "[Not collapsed]");

        for(int i = 0; i < state.length; i++)
            stream.println(String.format("%d: %s", i, state[i]));
    }
}
