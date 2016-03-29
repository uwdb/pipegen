package org.brandonhaynes.pipegen.dataflow;

import com.google.common.collect.Sets;
import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JNewExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        final Set<String> ioStatements = Sets.newHashSet("new java.lang.StringBuilder");

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.dataFlowTransform", new BodyTransformer() {
                    protected void internalTransform(Body body, String phase, Map options) {
                        new DataFlowAnalysis(new ExceptionalUnitGraph(body), ioStatements, Main::transform);
                    }
                }));

        String[] args2 = new String[9];
        //args2[0] = args[0];
        args2[0] = "-cp";
        args2[1] = ".:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:lib/btrace-boot.jar:lib/hadoop-core-1.2.1.jar:/target/classes";
        args2[2] = "-i";
        args2[3] = "jtp";
        args2[4] = "--allow-phantom-refs";
        args2[5] = "-p";
        args2[6] = "jb";
        args2[7] = "use-original-names";
        args2[8] = "org.brandonhaynes.pipegen.dataflow.IOTest"; //"org.apache.hadoop.fs.Path";

        soot.Main.main(args2);
    }

    private static void transform(Unit node, Set<Unit> input) {
        if(node instanceof JAssignStmt) {
            JAssignStmt assign = (JAssignStmt)node;
            if(assign.getRightOpBox().getValue().toString().equals("new java.lang.StringBuilder") &&
                    assign.getRightOpBox().getValue() instanceof JNewExpr) {
                JNewExpr expr = (JNewExpr)assign.getRightOpBox().getValue();
                assign.getRightOpBox().setValue(new JNewExpr(RefType.v("org.brandonhaynes.pipegen.instrumentation.injected.AugmentedStringBuilder")));
            }
        }

    }
}