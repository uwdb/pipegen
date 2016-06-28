package org.brandonhaynes.pipegen.instrumentation.injected.java;

import org.brandonhaynes.pipegen.utilities.ArrayUtilities;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.nio.charset.Charset;
import java.util.Locale;

public class AugmentedString extends org.brandonhaynes.pipegen.instrumentation.injected.java.String {
    private static final Integer INTEGER = 0;
    private static final Long LONG = 0l;
    private static final Byte BYTE = 0;
    private static final Float FLOAT = 0f;
    private static final Double DOUBLE= 0d;

    private String decoratedString = null;
    private Object[] state;
    private byte[] byteState;
    private int[] intState;
    private long[] longState;
    private float[] floatState;
    private double[] doubleState;

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
    public static AugmentedString decorate(Object o) {
        return new AugmentedString(o);
    }

    public static AugmentedString concat(AugmentedString left, byte right) {
        return new AugmentedString(ArrayUtilities.concat(left.state, BYTE),
                                   ArrayUtilities.concat(left.byteState, right),
                                   left.intState, left.longState, left.floatState, left.doubleState);
    }

    public static AugmentedString concat(AugmentedString left, int right) {
        return new AugmentedString(ArrayUtilities.concat(left.state, INTEGER), left.byteState,
                ArrayUtilities.concat(left.intState, right), left.longState, left.floatState, left.doubleState);
    }

    public static AugmentedString concat(AugmentedString left, long right) {
        return new AugmentedString(ArrayUtilities.concat(left.state, LONG), left.byteState, left.intState,
                                   ArrayUtilities.concat(left.longState, right), left.floatState,
                                   left.doubleState);
    }

    public static AugmentedString concat(AugmentedString left, float right) {
        return new AugmentedString(ArrayUtilities.concat(left.state, FLOAT), left.byteState, left.intState,
                                   left.longState, ArrayUtilities.concat(left.floatState, right), left.doubleState);
    }

    public static AugmentedString concat(AugmentedString left, double right) {
        return new AugmentedString(ArrayUtilities.concat(left.state, DOUBLE), left.byteState, left.intState,
                                   left.longState, left.floatState, ArrayUtilities.concat(left.doubleState, right));
    }

    public static AugmentedString concat(AugmentedString left, Object right) {
        if(right instanceof AugmentedString)
            return concat(left, (AugmentedString)right);
        else
            return new AugmentedString(ArrayUtilities.concat(left.state, right),
                                       left.byteState, left.intState, left.longState,
                                       left.floatState, left.doubleState);
    }

    public static AugmentedString concat(AugmentedString left, AugmentedString right) {
        Object[] newState = ArrayUtilities.concat(left.state, right.state);
        byte[] newBytes = ArrayUtilities.concat(left.byteState, right.byteState);
        int[] newInts = ArrayUtilities.concat(left.intState, right.intState);
        long[] newLongs = ArrayUtilities.concat(left.longState, right.longState);
        float[] newFloats = ArrayUtilities.concat(left.floatState, right.floatState);
        double[] newDoubles = ArrayUtilities.concat(left.doubleState, right.doubleState);

        return new AugmentedString(newState, newBytes, newInts, newLongs, newFloats, newDoubles);
    }

    public AugmentedString() {
        this.state = new Object[0];
        this.byteState = new byte[0];
        this.intState = new int[0];
        this.longState = new long[0];
        this.floatState = new float[0];
        this.doubleState = new double[0];
    }

    public AugmentedString(String s) {
        this();
        decoratedString = s;
    }

    public AugmentedString(int i) {
        this(INTEGER);
        this.intState = new int[1];
        this.intState[0] = i;
    }

    public AugmentedString(long i) {
        this(LONG);
        this.longState = new long[1];
        this.longState[0] = i;
    }

    public AugmentedString(byte b) {
        this(BYTE);
        this.byteState = new byte[1];
        this.byteState[0] = b;
    }

    public AugmentedString(float f) {
        this(FLOAT);
        this.floatState = new float[1];
        this.floatState[0] = f;
    }

    public AugmentedString(double d) {
        this(DOUBLE);
        this.doubleState = new double[1];
        this.doubleState[0] = d;
    }

    public AugmentedString(Object o) {
        this.byteState = new byte[0];
        this.intState = new int[0];
        this.longState = new long[0];
        this.floatState = new float[0];
        this.doubleState = new double[0];
        this.state = new Object[1];
        this.state[0] = o;
    }

    public AugmentedString(Object[] state) {
        this.state = state;
        this.byteState = new byte[0];
        this.intState = new int[0];
        this.longState = new long[0];
        this.floatState = new float[0];
        this.doubleState = new double[0];
    }

    private AugmentedString(Object[] state, byte[] bytes, int[] ints, long[] longs, float[] floats, double[] doubles) {
        this.state = state;
        this.byteState = bytes;
        this.intState = ints;
        this.longState = longs;
        this.floatState = floats;
        this.doubleState = doubles;
    }

    private String collapse() {
        if(!hasCollapsed()) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0, intIndex = 0, longIndex = 0, byteIndex = 0, floatIndex = 0, doubleIndex = 0;
                  i < state.length; i++)
                if(state[i] == INTEGER)
                    builder.append(intState[intIndex++]);
                else if(state[i] == BYTE)
                    builder.append(byteState[byteIndex++]);
                else if(state[i] == LONG)
                    builder.append(longState[longIndex++]);
                else if(state[i] == FLOAT)
                    builder.append(floatState[floatIndex++]);
                else if(state[i] == DOUBLE)
                    builder.append(doubleState[doubleIndex++]);
                else
                    builder.append(state[i]);
            decoratedString = builder.toString();
        }

        return decoratedString;
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
    public boolean contentEquals(java.lang.StringBuffer var1) {
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

    public void printState(PrintStream stream) {
        stream.println(hasCollapsed() ? "[Collapsed]" : "[Not collapsed]");

        for(int i = 0, intIndex = 0, longIndex = 0, byteIndex = 0, floatIndex = 0, doubleIndex = 0;
            i < state.length; i++)
            if(state[i] == INTEGER)
                stream.println(String.format("%d: %d (INT)", i, intState[intIndex++]));
            else if(state[i] == BYTE)
                stream.println(String.format("%d: %d (BYTE)", i, byteState[byteIndex++]));
            else if(state[i] == LONG)
                stream.println(String.format("%d: %d (LONG)", i, longState[longIndex++]));
            else if(state[i] == FLOAT)
                stream.println(String.format("%d: %f (FLOAT)", i, floatState[floatIndex++]));
            else if(state[i] == DOUBLE)
                stream.println(String.format("%d: %f (DOUBLE)", i, doubleState[doubleIndex++]));
            else
                stream.println(String.format("%d: %s", i, state[i]));
    }
}
