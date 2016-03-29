package org.brandonhaynes.pipegen.utilities;

import javassist.*;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

public class StartupDelay {
    private final static String SENTINEL_CLASSNAME = "org.brandonhaynes.pipegen.sentinels.SentinelClass";
    private final static String STARTUP_DELAY_STATEMENT = "{ java.lang.Thread.sleep(5000L); }";

    public static boolean addStartupDelay(File jar, String className, String method)
            throws NotFoundException, CannotCompileException, IOException {
        return addStartupDelay(ClassPool.getDefault(), jar, className, method);
    }

    public static boolean addStartupDelay(ClassPool pool, File jar, String className, String method)
            throws NotFoundException, CannotCompileException, IOException {
        pool.insertClassPath(jar.toString());
        CtClass augmentedClass = pool.get(className);
        boolean hasClass = hasClass(pool, className);

        if(!hasClass) {
            CtClass sentinelClass = pool.get(SentinelClass.class.getName());
            CtMethod augmentedMethod = augmentedClass.getDeclaredMethod(method);

            sentinelClass.setName(SENTINEL_CLASSNAME);
            augmentedMethod.insertBefore(STARTUP_DELAY_STATEMENT);

            replaceClass(augmentedClass, augmentedClass);
            replaceClass(augmentedClass, sentinelClass);
        }

        return !hasClass;
    }

    private static void replaceClass(CtClass jarClass, CtClass clazz) throws IOException, NotFoundException, CannotCompileException {
        URL jarUrl = ((JarURLConnection)jarClass.getURL().openConnection()).getJarFileURL();
        JarFile jar = new JarFile(jarUrl.getFile());
        JarUpdater.replaceFile(jar, toClassPath(clazz), clazz.toBytecode());
        jar.close();
    }

    private static File toClassPath(CtClass cc) {
        return new File(cc.getName().replace(".", File.separator) + ".class");
    }

    private static boolean hasClass(ClassPool pool, String className) {
        try {
            pool.get(className);
            return true;
        } catch(NotFoundException e) {
            return false;
        }
    }

    class SentinelClass {
    }
}