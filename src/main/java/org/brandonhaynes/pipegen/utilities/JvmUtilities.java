package org.brandonhaynes.pipegen.utilities;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.Modifier;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.mutation.ClassModifierReplacer;

import java.io.IOException;

public class JvmUtilities {
    public static void main(String[] args) throws IOException {
        if(args.length != 2)
            System.out.println("Usage: JvmUtilities RemoveFinalFlagFromString class-name");
        else
            for(String className: args[1].split(",")) {
                System.out.println(className);
                removeFinalFlag(className);
            }
    }

    public static void removeFinalFlag(String className) throws IOException {
        try {
            ClassModifierReplacer.setModifiers(new ClassPool(true), className, Modifier.PUBLIC);
        } catch(NotFoundException | CannotCompileException e) {
            throw new IOException(e);
        }
    }
}
