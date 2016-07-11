package org.brandonhaynes.pipegen.templates;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.btrace.annotations.*;
import com.sun.btrace.AnyType;
import org.apache.hadoop.fs.Path;

import static com.sun.btrace.BTraceUtils.*;

@BTrace(unsafe=true)
public class ImportTracer {
    @OnMethod(clazz="org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileInputStream",
            method="<init>",
            location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnFileInputStream(@Self Object self, @TargetMethodOrField String method,
                                         @ProbeMethodName String probeMethod, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }
    
    //region Adapted from BTraceUtils / BTraceRuntime

    public static void printArray(StringBuilder buf, Object[] array) {
        //StringBuilder buf = new StringBuilder(prefix);
        buf.append('[');
        for (Object obj : array) {
            buf.append(Strings.str(obj));
            buf.append(", ");
        }
        buf.append(']').append(LINE_SEPARATOR);
    }

    public static void printFields(StringBuilder buffer, Object obj) {
        printFields(obj, false, buffer);
    }

    public static void printFields(Object obj, boolean classNamePrefix, StringBuilder buf) {
        buf.append('{');
        addFieldValues(buf, obj, obj.getClass(), classNamePrefix);
        buf.append('}').append(LINE_SEPARATOR);
        //println(buf.toString());
    }

    private static void addFieldValues(StringBuilder buf, Object obj,
                                       final Class<?> clazz, boolean classNamePrefix) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
        }

        //Field[] fields = getAllFields();
        for (Field f : fields) {
            int modifiers = f.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                if (classNamePrefix) {
                    buf.append(f.getDeclaringClass().getName());
                    buf.append('.');
                }
                buf.append(f.getName());
                buf.append('=');
                try {
                    buf.append(Strings.str(f.get(obj)));
                } catch (Exception exp) {
                    throw translate(exp);
                }
                buf.append(", ");
            }
        }
        Class<?> sc = clazz.getSuperclass();
        if (sc != null) {
            addFieldValues(buf, obj, sc, classNamePrefix);
        }
    }

    private static RuntimeException translate(Exception exp) {
        if (exp instanceof RuntimeException) {
            return (RuntimeException) exp;
        } else {
            return new RuntimeException(exp);
        }
    }

    public static void jstack(StringBuilder buffer) {
        jstack(1, -1, buffer);
    }

    private static void jstack(int strip, int numFrames, StringBuilder buffer) {
        if (numFrames == 0) return;
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        stackTrace(st, strip + 2, numFrames, buffer);
    }

    private static final int THRD_DUMP_FRAMES = 1;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static void stackTrace(StackTraceElement[] st,
                           int strip, int numFrames, StringBuilder buffer) {
        stackTrace(null, st, strip, numFrames, buffer);
    }

    private static void stackTrace(String prefix, StackTraceElement[] st,
                                   int strip, int numFrames, StringBuilder buf) {
        boolean printWarning = true;
        strip = strip > 0 ? strip + THRD_DUMP_FRAMES : 0;
        numFrames = numFrames > 0 ? numFrames : st.length - strip;

        int limit = strip + numFrames;
        limit = limit <= st.length ? limit : st.length;

        if (prefix == null) { prefix = ""; }

        //StringBuilder buf = new StringBuilder();
        for (int i = strip; i < limit; i++) {
            buf.append(prefix);
            buf.append(st[i].toString());
            buf.append(LINE_SEPARATOR);
        }
        if (printWarning && limit < st.length) {
            buf.append(prefix);
            buf.append(st.length - limit);
            buf.append(" more frame(s) ...");
            buf.append(LINE_SEPARATOR);
        }
    }
    //endregion
}
