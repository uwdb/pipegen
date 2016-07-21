package org.brandonhaynes.pipegen.templates;

import com.sun.btrace.AnyType;
import org.apache.hadoop.fs.Path;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.sun.btrace.BTraceUtils.*;

@BTrace(unsafe=true)
public class ImportTracer {
    @OnMethod(clazz="+java.lang.Readable",
            //@OnMethod(clazz="+org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedWriter",
            method="read")
//              location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnInterceptedReaderRead(@Self Object self, AnyType[] args) {
        if(self.getClass().getName().equals("org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedReader")) {
            StringBuilder buffer = new StringBuilder();

            buffer.append("Entry:").append(LINE_SEPARATOR);
            buffer.append(classOf(self)).append(LINE_SEPARATOR);
            buffer.append(probeLine()).append(LINE_SEPARATOR);
            printArray(buffer, new AnyType[0]);
            printFields(buffer, self);
            jstack(buffer);

            println(buffer.toString());
        }
    }

    @OnMethod(clazz="+java.lang.Appendable",
    //@OnMethod(clazz="+org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedWriter",
              method="append")
//              location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnInterceptedWriterWrite(@Self Object self, AnyType[] args) {
        if(self.getClass().getName().equals("org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedBufferedWriter")) {
            StringBuilder buffer = new StringBuilder();

            buffer.append("Entry:").append(LINE_SEPARATOR);
            buffer.append(classOf(self)).append(LINE_SEPARATOR);
            buffer.append(probeLine()).append(LINE_SEPARATOR);
            printArray(buffer, new AnyType[0]);
            printFields(buffer, self);
            jstack(buffer);

            println(buffer.toString());
        }
    }

    @OnMethod(clazz="+java.io.FileOutputStream",
              method="write")
              //location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnInterceptedFileOutputStreamWrite(@Self Object self, //@TargetMethodOrField String method,
                                                          //@ProbeMethodName String probeMethod,
                                                          AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }

//    @OnMethod(clazz="/.*/", method="toString")
//    public static void OnToString(@Self Object self, AnyType[] args) {
//        print("Entry:\n" + classOf(self) + "\n" + "\n" + "[]\n" + "{}\n" + jstackStr());
//    }
    @OnMethod(clazz="org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileInputStream",
            method="<init>",
            location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnInterceptedFileInputStream(@Self Object self, @TargetMethodOrField String method,
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


    @OnMethod(clazz="+java.io.FileInputStream",
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

    @OnMethod(clazz="+java.io.InputStreamReader",
            method="<init>",
            location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnInputStreamReader(@Self Object self, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }

    @OnMethod(clazz="+java.io.BufferedReader",
            method="<init>")
            //location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnBufferedReader(@Self Object self, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }

    @OnMethod(clazz="+java.io.FileOutputStream",
            method="<init>") //,
            //location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnFileOutputStream(@Self Object self, //@TargetMethodOrField String method),
                                          //@ProbeMethodName String probeMethod,
                                          AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }

    @OnMethod(clazz="+java.io.OutputStreamWriter",
            method="<init>",
            location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnOutputStreamWriter(@Self Object self, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }

    @OnMethod(clazz="+java.io.BufferedWriter",
            method="<init>",
            location=@Location(value=Kind.CALL, clazz="/.*/", method="/.*/"))
    public static void OnBufferedWriter(@Self Object self, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, self);
        jstack(buffer);

        println(buffer.toString());
    }

    /*
    @OnMethod(clazz="org.apache.hadoop.fs.FileSystem",
    //@OnMethod(clazz="org.apache.hadoop.hdfs.DistributedFileSystem",
    //@OnMethod(clazz="org.apache.hadoop.fs.RawLocalFileSystem",
    //@OnMethod(clazz="org.apache.hadoop.fs.LocalFileSystem",
            method="open")
    public static void OnHadoopFileSystemOpen(@Self Object self, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        //printFields(buffer, self);
        printFields(buffer, args[0] instanceof Path ? args[0] : self);
        jstack(buffer);

        println(buffer.toString());
    }
*/

    @OnMethod(clazz="org.apache.hadoop.fs.FileSystem",
            method="create")
    public static void OnHadoopFileSystemCreate(@Self Object self, AnyType[] args) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("Entry:").append(LINE_SEPARATOR);
        buffer.append(classOf(self)).append(LINE_SEPARATOR);
        buffer.append(probeLine()).append(LINE_SEPARATOR);
        printArray(buffer, args);
        printFields(buffer, args[0] instanceof Path ? args[0] : self);
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
                    buf.append(Strings.str(f.get(obj)).replace("\n", "\\n"));
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
