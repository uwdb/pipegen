package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.optimization.sinks.InvokeMethodSinkExpression;
import org.brandonhaynes.pipegen.optimization.sinks.IoSinkExpressions;
import org.brandonhaynes.pipegen.optimization.transforms.StringExpressionTransformer;
import org.brandonhaynes.pipegen.utilities.PathUtilities;
import soot.*;
import soot.options.Options;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

//TODO optimizer should defer to SootUtilities
public class Optimizer {
    public static Scene optimize(CompileTimeConfiguration configuration, Set<StackFrame> stackFrames) {
        resetSoot();
        configureSoot(configuration, null);
        //TODO this selects all overloads and is a superset of the actual methods we want
        Collection<String> s = stackFrames.stream().flatMap(f -> Scene.v().getSootClass(f.getClassName()).getMethods().stream().filter(m -> m.getName().equals(f.getMethodName())))
                .map(SootMethod::getSignature).collect(Collectors.toSet());
        return executeSoot(s);
    }

    public static Scene optimize(CompileTimeConfiguration configuration, Collection<String> signatures) {
        return optimize(configuration, signatures, null);
    }

    public static Scene optimize(CompileTimeConfiguration configuration, Collection<String> signatures,
                                String entryPoint) {
        resetSoot();
        configureSoot(configuration, entryPoint);
        return executeSoot(signatures);
    }

    private static void resetSoot() {
        G.reset();
    }

    //TODO parameterize with scene
    private static Scene executeSoot(Collection<String> methodSignatures) {
        IoSinkExpressions sinkExpressions = new IoSinkExpressions(Scene.v());

        Set<MethodAnalysis> processed = Sets.newHashSet();
        Queue<MethodAnalysis> queue = Queues.newArrayDeque(methodSignatures.stream()
                                                                           .map(s -> Scene.v().getMethod(s))
                                                                           .map(MethodAnalysis::new)
                                                                           .collect(Collectors.toSet()));

        PackManager.v().getPack("wjtp").add(
                new Transform("wjtp.dataFlowTransform", new SceneTransformer() {
                    @Override protected void internalTransform(String phaseName, Map options) {
                        processQueue(queue, processed, sinkExpressions);
                    }
                }));

        PackManager.v().runPacks();
        PackManager.v().writeOutput();
        return Scene.v();
    }

    private static void processQueue(Queue<MethodAnalysis> queue, Set<MethodAnalysis> processed,
                                     IoSinkExpressions sinkExpressions) {
        while(!queue.isEmpty()) {
            MethodAnalysis analysis = queue.remove();
            processed.add(analysis);
            if(analysis.getCallee() != null) //TODO do this inside sink
                sinkExpressions.add(new InvokeMethodSinkExpression(analysis.getCallee()));
            new DataFlowAnalysis(queue, processed, analysis, sinkExpressions, new StringExpressionTransformer());
        }
    }

    private static void configureSoot(CompileTimeConfiguration configuration, String entryPoint) {
        Set<String> classPaths = configuration.optimizationConfiguration.getClassPaths().stream()
                                              .flatMap(PathUtilities::getJavaFiles)
                                              .map(Path::toString)
                                              .collect(Collectors.toSet());
        Options.v().set_output_format(Options.output_format_class);

        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);

        Options.v().set_whole_program(true);
        Options.v().set_no_bodies_for_excluded(true);

        Options.v().set_prepend_classpath(true);
        Options.v().set_include(Lists.newArrayList("jtp"));

        if(entryPoint != null)
            Options.v().set_main_class(entryPoint);
        else
            Options.v().setPhaseOption("cg", "all-reachable:true");

        Options.v().set_allow_phantom_refs(true);
        Options.v().set_asm_backend(true);

        Options.v().set_soot_classpath(String.join(":", classPaths));

        Options.v().set_process_dir(configuration.optimizationConfiguration.getClassPaths().stream()
                                                                           .flatMap(PathUtilities::getJavaFiles)
                                                                           .map(Path::toString)
                                                                           .collect(Collectors.toList()));
        Scene.v().loadNecessaryClasses();
    }
}
