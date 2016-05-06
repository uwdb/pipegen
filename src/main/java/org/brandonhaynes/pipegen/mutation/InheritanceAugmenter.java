package org.brandonhaynes.pipegen.mutation;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

import javassist.CtClass;
import javassist.ClassPool;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.utilities.JarUpdater;

public class InheritanceAugmenter {
    public static void interceptInherited(String interceptedClassName, Class newSuperClass, File[] classPaths)
            throws IOException, NotFoundException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        for (File classPath : classPaths) pool.insertClassPath(classPath.getName());
        interceptInherited(pool, pool.get(interceptedClassName), newSuperClass);
    }

    //public static void interceptInherited(Class interceptedClass, Class newSuperClass)
    // throws IOException, NotFoundException, CannotCompileException {
    //	ClassPool pool = ClassPool.getDefault();
    //	pool.insertClassPath(new ClassClassPath(interceptedClass));
    //	interceptInherited(pool, pool.get(interceptedClass.getName()), newSuperClass);
    //}

    private static void interceptInherited(ClassPool pool, CtClass interceptedClass, Class newSuperClass)
            throws IOException, NotFoundException, CannotCompileException {
        interceptedClass.defrost();
        interceptedClass.setSuperclass(pool.get(newSuperClass.getName()));
        interceptInherited(interceptedClass);
    }

    private static void interceptInherited(CtClass augmentedClass)
            throws IOException, NotFoundException, CannotCompileException {
        if (augmentedClass.getURL().getProtocol().equals("jar")) {
            interceptInherited(augmentedClass,
                               ((JarURLConnection) augmentedClass.getURL().openConnection()).getJarFileURL());
        } else
            throw new NotFoundException("Augmenting classes not within a JAR is currently not supported.");
    }

    private static void interceptInherited(CtClass augmentedClass, URL jarURL)
            throws IOException, NotFoundException, CannotCompileException {
        if (jarURL.getProtocol().equals("file")) {
            JarFile jar = new JarFile(jarURL.getFile());
            injectClass(jar, new CtClass[]{augmentedClass, augmentedClass.getSuperclass()});
        } else
            throw new NotFoundException("Augmenting a JAR not on the local file system is not currently supported.");
    }

    //private static void injectClass(JarFile jar, ClassPool pool, Class<?>[] classes)
    // throws IOException, NotFoundException, CannotCompileException {
    //	for(Class clazz: classes)
    //		injectClass(jar, pool, clazz);
    //}

    private static void injectClass(JarFile jar, CtClass[] classes)
            throws IOException, NotFoundException, CannotCompileException {
        for (CtClass clazz : classes)
            injectClass(jar, clazz);
    }

    //private static void injectClass(JarFile jar, ClassPool pool, Class clazz)
    // throws IOException, NotFoundException, CannotCompileException {
    //	injectClass(jar, pool.get(clazz.getName()));
    //}

    private static void injectClass(JarFile jar, CtClass clazz)
            throws IOException, CannotCompileException {
        JarUpdater.replaceClass(jar, toClassPath(clazz), clazz.toBytecode());
    }

    private static File toClassPath(CtClass cc) {
        return new File(cc.getName().replace(".", File.separator) + ".class");
    }
}