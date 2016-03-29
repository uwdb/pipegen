package org.brandonhaynes.pipegen.mutation;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import org.brandonhaynes.pipegen.utilities.JarUpdater;

import java.io.IOException;
import java.util.logging.Logger;

public class ExpressionReplacer {
    private static final Logger log = Logger.getLogger(ExpressionReplacer.class.getName());

    public static void replaceExpression(String className, String methodName, int line,
                                            String replacementExpression, ClassPool pool)
            throws IOException, NotFoundException, CannotCompileException {
        replaceExpression(pool.get(className), methodName, line, replacementExpression);
    }

    private static void replaceExpression(CtClass targetClass, String methodName,
                                             int line, String replacementExpression)
            throws IOException, NotFoundException, CannotCompileException {
        replaceExpression(targetClass,
                isConstructor(methodName)
                    ? targetClass.getConstructors()[0] //TODO this will break with multiple constructors
                    : targetClass.getDeclaredMethod(methodName), line, replacementExpression);
    }

    private static void replaceExpression(CtClass clazz, CtBehavior constructor, int line, String replacementExpression)
            throws IOException, NotFoundException, CannotCompileException {
        clazz.defrost();
        constructor.instrument(new ExprEditor() {
            public void edit(NewExpr expression) throws CannotCompileException {
                if(expression.getLineNumber() == line) {
                    log.info("Modifying " + clazz.toString());
                    expression.replace(replacementExpression);
                }
            }
        });

        if(clazz.isModified())
            JarUpdater.replaceClass(clazz.getClassPool().find(clazz.getName()), clazz);
    }

    private static boolean isConstructor(String name) {
        return name.equals("<init>");
    }
}