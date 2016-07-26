package org.brandonhaynes.pipegen.utilities;

public class ThreadUtilities {
    public static void UncheckedSleep(long duration) {
        try {
            java.lang.Thread.sleep(duration);
        } catch(InterruptedException ignored) {
        }
    }
}
