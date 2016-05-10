package org.brandonhaynes.pipegen.utilities;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.mutation.ClassModifierReplacer;
import org.brandonhaynes.pipegen.mutation.SuperClassReplacer;
import soot.Modifier;

import java.io.IOException;

public class ClassUtilities {
    public static void RemoveFinalFlagFromString() throws IOException {
        try {
            ClassModifierReplacer.setModifiers(new ClassPool(true), "java.lang.String", Modifier.PUBLIC);
        } catch(NotFoundException| CannotCompileException e) {
            throw new IOException(e);
        }
    }

    public static void ChangeAugmentedStringSuperClass(ClassPool pool) throws IOException {
        try {
            SuperClassReplacer.setSuperClass(pool, AugmentedString.class.getName(), String.class.getName());
        } catch(NotFoundException| CannotCompileException e) {
            throw new IOException(e);
        }
    }
}
