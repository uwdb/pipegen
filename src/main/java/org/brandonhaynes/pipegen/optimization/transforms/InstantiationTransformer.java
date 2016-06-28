package org.brandonhaynes.pipegen.optimization.transforms;

import soot.RefType;
import soot.Scene;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;

import java.util.Set;

public class InstantiationTransformer implements ExpressionTransformer {
    private final Class<?> targetClass;
    private final Class<?> replacementClass;

    public InstantiationTransformer(Class<?> targetClass, Class<?> replacementClass) {
        this.targetClass = targetClass;
        this.replacementClass = replacementClass;
    }

    @Override
    public boolean isApplicable(Set<Unit> input, Unit node, Set<Unit> output) {
        return node instanceof AssignStmt && isApplicable((AssignStmt) node);
    }

    private boolean isApplicable(AssignStmt node) {
        return node.getRightOp() instanceof NewExpr && isApplicable((NewExpr)node.getRightOp());
    }

    private boolean isApplicable(NewExpr node) {
        return node.getType() == Scene.v().getType(targetClass.getName());
    }

    @Override
    public void transform(Set<Unit> input, Unit node, Set<Unit> output, CompositeExpressionTransformer transforms) {
        transform((NewExpr)((AssignStmt)node).getRightOp());
    }

    public void transform(NewExpr instantiation) {
        instantiation.setBaseType((RefType)Scene.v().getType(replacementClass.getName()));
    }
}
