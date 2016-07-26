package org.brandonhaynes.pipegen.utilities;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.*;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptMetadata;
import org.brandonhaynes.pipegen.instrumentation.injected.utility.InterceptUtilities;
import org.brandonhaynes.pipegen.mutation.ClassModifierReplacer;
import org.brandonhaynes.pipegen.mutation.SuperClassReplacer;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryClient;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryEntry;
import soot.Modifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassUtilities {
    public static void main(String[] args) throws IOException {
        if(args.length != 2)
            System.out.println("Usage: ClassUtilities RemoveFinalFlagFromString class-name");
        else
            removeFinalFlagFromString(args[1]);
    }

    public static void removeFinalFlagFromString(String className) throws IOException {
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
}
