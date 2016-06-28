package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedOutputStreamWriter;
import org.brandonhaynes.pipegen.support.MockInterceptedOutputStreamWriter;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class OptimizationTest {
    protected static InterceptedOutputStreamWriter getTestWriter() throws IOException {
        FileOutputStream stream = InterceptedFileOutputStream.intercept(File.createTempFile("test", null));
        return new MockInterceptedOutputStreamWriter(stream);
    }

    public Path getConfiguration() {
        return Paths.get("/home/bhaynes/research/pipegen/src/test/resources/optimization.yaml"); //TODO
    }

    public String getSignature() {
        return String.format("<%s: void %s>", getClass().getName(), getDataflowMethodSignature());
    }

    protected Path getOutputPath() {
        return Paths.get("/home/bhaynes/research/pipegen/.idea/modules/sootOutput"); //TODO
    }

    protected String getInvocationMethodName() {
        return "test";
    }
    protected String getDataflowMethodSignature() {
        return "test()";
    }

    public void testTransformedClass()
            throws MalformedURLException, ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        testTransformedClass(getOutputPath());
    }

    public void testTransformedClass(Path transformedClassPath)
            throws MalformedURLException, ClassNotFoundException, NoSuchMethodException,
                   IllegalAccessException, InstantiationException, InvocationTargetException {
        ClassLoader loader = new URLClassLoader(new URL[] {transformedClassPath.toUri().toURL()}, null);
        Class<?> clazz = loader.loadClass(getClass().getName());
        Object instance = clazz.newInstance();
        Method method = clazz.getDeclaredMethod(getInvocationMethodName());
        method.invoke(instance);
    }

    @Test
    public void optimizationTest() throws Exception {
        Optimizer.optimize(new CompileTimeConfiguration(getConfiguration()),
                           Lists.newArrayList(getSignature()));
        testTransformedClass();
    }

    protected abstract void test() throws IOException;
}
