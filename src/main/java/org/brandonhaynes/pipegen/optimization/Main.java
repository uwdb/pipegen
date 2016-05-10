package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.optimization.sinks.IoSinkExpressions;
import org.brandonhaynes.pipegen.optimization.transforms.StringExpressionTransformer;
import soot.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JNewExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.dataFlowTransform", new BodyTransformer() {
                    protected void internalTransform(Body body, String phase, Map options) {
                        new DataFlowAnalysis(new ExceptionalUnitGraph(body),
                                             IoSinkExpressions.getAll(),
                                             StringExpressionTransformer.getAll());
                    }
                }));

           //String[] args2 = new String[10];
        List<String> args2 = Lists.newArrayList();
        //args2[0] = args[0];
        args2.add("-cp");
        args2.add(".:lib/soot-trunk.jar:lib/jsr305-1.3.9.jar:lib/javassist-3.12.1.GA.jar:lib/btrace-boot.jar:lib/btrace-client-1.3.4.jar:lib/commons-logging-1.1.3.jar:/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar:lib/commons-lang3-3.4.jar:lib/jackson-databind-2.7.2.jar:lib/jackson-core-2.7.2.jar:lib/jackson-annotations-2.7.0.jar:lib/jackson-dataformat-yaml-2.7.2.jar:lib/hadoop-core-1.2.1.jar:lib/commons-io-2.1.jar");
        //args2.add(".:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:lib/btrace-boot.jar:lib/hadoop-core-1.2.1.jar:lib/soot-trunk.jar:target/production/pipegen/org/brandonhaynes/pipegen/instrumentation/injected/java/:target/production/pipegen");
        //args2.add(".:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:lib/btrace-boot.jar:lib/hadoop-core-1.2.1.jar:/target/classes");
        //args2.add("-java-version");
        //args2.add("1.8");
        //args2.add("-app");
        args2.add("-process-dir");
        args2.add("target/production/pipegen");

        args2.add("-pp");
        args2.add("-i");
        args2.add("jtp");
        args2.add("--allow-phantom-refs"); //TODO really shouldn't have this enabled...
        args2.add("-asm-backend");
        //args2.add("-p");
        //args2.add("jb");
        //args2.add("use-original-names:true");
        args2.add("org.brandonhaynes.pipegen.dataflow.IOTest"); //"org.apache.hadoop.fs.Path";

        //soot.Main.main(args2.toArray(new String[args2.size()]));
        Optimizer.optimize(new CompileTimeConfiguration("systems/myria.yaml"));
    }

    /*
    public static void configure(String classpath) {
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_soot_classpath(classpath);
        Options.v().set_prepend_classpath(true);

        PhaseOptions.v().setPhaseOption("bb", "off");
        PhaseOptions.v().setPhaseOption("tag.ln", "on");
        PhaseOptions.v().setPhaseOption("jj.a", "on");
        PhaseOptions.v().setPhaseOption("jj.ule", "on");

        Options.v().set_whole_program(true);
    }

    private static void foo() {
        final Set<String> ioStatements = Sets.newHashSet("new java.lang.StringBuilder");

        configure(".");
        SootClass sootClass = Scene.v().loadClassAndSupport("className");
        sootClass.setApplicationClass();

        // Retrieve the method and its body
        SootMethod m = sootClass.getMethodByName("methodName");
        Body b = m.retrieveActiveBody();

        // Instruments bytecode
        Transform t = new Transform("jtp.dataFlowTransform", new BodyTransformer() {
            protected void internalTransform(Body body, String phase, Map options) {
                log.info("....");
                new DataFlowAnalysis(new ExceptionalUnitGraph(body), ioStatements, Main::transform);
            } });
        //t.transform(b);
    }
    */

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