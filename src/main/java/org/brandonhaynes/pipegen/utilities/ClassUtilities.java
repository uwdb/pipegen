package org.brandonhaynes.pipegen.utilities;

import com.codahale.metrics.Metric;
import com.google.common.collect.Lists;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ArrowBuf;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.arrow.vector.ZeroVector;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.*;
import org.brandonhaynes.pipegen.instrumentation.injected.hadoop.InterceptedFSDataInputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.hadoop.InterceptedTextInputFormat;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedResultSet;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuffer;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuilder;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.mutation.SuperClassReplacer;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.io.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassUtilities {
    public static void changeAugmentedStringSuperClass(ClassPool pool) throws IOException {
        try {
            SuperClassReplacer.setSuperClass(pool, AugmentedString.class.getName(), String.class.getName());
        } catch(NotFoundException| CannotCompileException e) {
            throw new IOException(e);
        }
    }

    public static List<Class> getPipeGenDependencies() {
        return new ArrayList<Class>() {{
            add(InterceptedFileOutputStream.class);
            add(InterceptedFileInputStream.class);
            add(InterceptedOutputStreamWriter.class);
            add(InterceptedBufferedWriter.class);
            add(InterceptedInputStreamReader.class);
            add(InterceptedBufferedReader.class);
            add(InterceptedBufferedOutputStream.class);
            add(InterceptedFileChannel.class);

            add(InterceptedTextInputFormat.class);
            add(InterceptedFSDataInputStream.class);

            add(OptimizedInterceptedFileOutputStream.class);
            add(OptimizedInterceptedFileInputStream.class);
            add(OptimizedInterceptedOutputStreamWriter.class);
            add(OptimizedInterceptedBufferedWriter.class);
            add(OptimizedInterceptedInputStreamReader.class);
            add(OptimizedInterceptedBufferedReader.class);
            add(OptimizedInterceptedBufferedOutputStream.class);

            add(InterceptUtilities.class);
            add(InterceptMetadata.class);
            add(RuntimeConfiguration.class);
            add(WorkerDirectoryClient.class);
            add(WorkerDirectoryEntry.class);
            add(Direction.class);

            add(org.brandonhaynes.pipegen.instrumentation.injected.java.String.class);
            add(org.brandonhaynes.pipegen.instrumentation.injected.java.StringBuffer.class);
            add(org.brandonhaynes.pipegen.instrumentation.injected.java.StringBuilder.class);
            add(AugmentedString.class);
            add(AugmentedStringBuilder.class);
            add(AugmentedStringBuffer.class);
            add(AugmentedResultSet.class);
            add(ArrayUtilities.class);
            add(ColumnUtilities.class);
            add(StreamUtilities.class);
            add(StringUtilities.class);
            add(PathUtilities.class);
            add(ThreadUtilities.class);
            add(CompositeVector.class);
            add(CompositeVector.Accessor.class);
            add(CompositeVector.Mutator.class);
            add(CompositeVector.Reader.class);
            
            JarUtilities.getClasses(getJarPath(Metric.class), false).forEach(this::add);
            JarUtilities.getClasses(getJarPath(ArrowBuf.class), false).forEach(this::add);
            JarUtilities.getClasses(getJarPath(ZeroVector.class), false).forEach(this::add);
            JarUtilities.getClasses(getJarPath(AbstractByteBuf.class), false).forEach(this::add);
        }};
    }

    public static Set<SootClass> getOriginalUnaugmentedClasses(Scene scene) {
        return new ArrayList<Class>() {{
            add(FileOutputStream.class);
            add(FileInputStream.class);
            add(OutputStreamWriter.class);
            add(BufferedWriter.class);
            add(InputStreamReader.class);
            add(BufferedReader.class);
            add(BufferedOutputStream.class);

            add(String.class);
            add(StringBuffer.class);
            add(StringBuilder.class);
            add(ResultSet.class);
        }}.stream().map(Class::getName).map(scene::getSootClass).collect(Collectors.toSet());
    }

    public static Stream<SootMethod> getClassHierarchyMethods(SootClass clazz) {
        return getClassHierarchy(clazz, Lists.newArrayList(clazz)).flatMap(c -> c.getMethods().stream());
    }

    public static Stream<SootClass> getClassHierarchy(SootClass clazz) {
        return getClassHierarchy(clazz, Lists.newArrayList(clazz));
    }

    private static Stream<SootClass> getClassHierarchy(SootClass clazz, Collection<SootClass> classes) {
        if(clazz.getSuperclass().getName().equals(Object.class.getName()))
            return classes.stream();
        else {
            classes.add(clazz.getSuperclass());
            clazz.getInterfaces().forEach(classes::add);
            return getClassHierarchy(clazz.getSuperclass(), classes);
        }
    }

    public static boolean isSameSubSignature(SootMethod left, SootMethod right) {
        return left.getSubSignature().equals(right.getSubSignature());
    }

    private static String getJarPath(Class representativeClass) {
        return representativeClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
