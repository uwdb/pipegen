package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
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
import org.brandonhaynes.pipegen.mutation.ClassModifierReplacer;
import org.brandonhaynes.pipegen.mutation.SuperClassReplacer;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import soot.Modifier;
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
    public static void main(String[] args) throws IOException {
        if(args.length != 2)
            System.out.println("Usage: ClassUtilities RemoveFinalFlagFromString class-name");
        else
            for(String className: args[1].split(","))
                removeFinalFlag(className);
    }

    public static void removeFinalFlag(String className) throws IOException {
        try {
            ClassModifierReplacer.setModifiers(new ClassPool(true), className, Modifier.PUBLIC);
        } catch(NotFoundException| CannotCompileException e) {
            throw new IOException(e);
        }
    }

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

            JarUtilities.getClasses("lib/metrics-core-3.0.1.jar", false).forEach(this::add);
            JarUtilities.getClasses("lib/arrow-memory-0.1-SNAPSHOT.jar", false).forEach(this::add);
            JarUtilities.getClasses("lib/vector-0.1-SNAPSHOT.jar", false).forEach(this::add);
            JarUtilities.getClasses("lib/netty-buffer-4.0.27.Final.jar", false).forEach(this::add);
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
}
