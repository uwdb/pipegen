package org.brandonhaynes.pipegen.utilities;

import java.lang.reflect.Array;

public class ArrayUtilities {
    public static Object[] concat(Object[] left, Object right) {
        Object[] joined = (Object[]) Array.newInstance(left.getClass().getComponentType(), left.length + 1);

        System.arraycopy(left, 0, joined, 0, left.length);
        joined[left.length] = right;

        return joined;
    }

    public static Object[] concat(Object[] left, Object[] right) {
        Object[] joined = (Object[])Array.newInstance(left.getClass().getComponentType(), left.length + right.length);

        System.arraycopy(left, 0, joined, 0, left.length);
        System.arraycopy(right, 0, joined, left.length, right.length);

        return joined;
    }

    public static byte[] concat(byte[] left, byte[] right) {
        byte[] joined = new byte[left.length + right.length];

        System.arraycopy(left, 0, joined, 0, left.length);
        System.arraycopy(right, 0, joined, left.length, right.length);

        return joined;
    }

    public static byte[] concat(byte[] left, byte right) {
        byte[] joined = new byte[left.length + 1];

        System.arraycopy(left, 0, joined, 0, left.length);
        joined[left.length] = right;

        return joined;
    }

    public static int[] concat(int[] left, int[] right) {
        int[] joined = new int[left.length + right.length];

        System.arraycopy(left, 0, joined, 0, left.length);
        System.arraycopy(right, 0, joined, left.length, right.length);

        return joined;
    }

    public static int[] concat(int[] left, int right) {
        int[] joined = new int[left.length + 1];

        System.arraycopy(left, 0, joined, 0, left.length);
        joined[left.length] = right;

        return joined;
    }

    public static float[] concat(float[] left, float[] right) {
        float[] joined = new float[left.length + right.length];

        System.arraycopy(left, 0, joined, 0, left.length);
        System.arraycopy(right, 0, joined, left.length, right.length);

        return joined;
    }

    public static float[] concat(float[] left, float right) {
        float[] joined = new float[left.length + 1];

        System.arraycopy(left, 0, joined, 0, left.length);
        joined[left.length] = right;

        return joined;
    }

    public static double[] concat(double[] left, double[] right) {
        double[] joined = new double[left.length + right.length];

        System.arraycopy(left, 0, joined, 0, left.length);
        System.arraycopy(right, 0, joined, left.length, right.length);

        return joined;
    }

    public static double[] concat(double[] left, double right) {
        double[] joined = new double[left.length + 1];

        System.arraycopy(left, 0, joined, 0, left.length);
        joined[left.length] = right;

        return joined;
    }
}
