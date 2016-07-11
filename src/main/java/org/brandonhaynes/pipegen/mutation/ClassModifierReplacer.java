package org.brandonhaynes.pipegen.mutation;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.Version;
import org.brandonhaynes.pipegen.utilities.JarClassPath;
import org.brandonhaynes.pipegen.utilities.JarUtilities;

import java.io.IOException;
import java.net.URL;

public class ClassModifierReplacer {
    public static void setModifiers(URL jarLocation, String className, int modifiers)
            throws IOException, NotFoundException, CannotCompileException {
        ClassPool pool = new ClassPool(false);
        pool.appendSystemPath();
        pool.insertClassPath(new JarClassPath(jarLocation));
        setModifiers(pool, className, modifiers);
    }

    public static void setModifiers(ClassPool pool, String className, int modifiers)
            throws IOException, NotFoundException, CannotCompileException {
        setModifiers(pool.find(className), pool.get(className), modifiers);
    }

    public static void setModifiers(URL jarLocation, CtClass cc, int modifiers)
            throws IOException, NotFoundException, CannotCompileException {
        cc.defrost();
        cc.setModifiers(modifiers);
        JarUtilities.replaceClass(jarLocation, cc,
                new Version(cc.getClassFile2().getMajorVersion(), cc.getClassFile2().getMinorVersion()), null);
    }
}