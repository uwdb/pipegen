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
    public static void main(String[] args) throws IOException {
        if(args.length != 2)
            System.out.println("Usage: ClassUtilities RemoveFinalFlagFromString class-name");
        else
            RemoveFinalFlagFromString(args[1]);
    }

    public static void RemoveFinalFlagFromString(String className) throws IOException {
        try {
            ClassModifierReplacer.setModifiers(new ClassPool(true), className, Modifier.PUBLIC);
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
