package org.brandonhaynes.pipegen.optimization;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.optimization.sinks.IoSinkExpressions;
import org.brandonhaynes.pipegen.optimization.transforms.StringExpressionTransformer;
import soot.*;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Optimizer {
    public static void optimize(CompileTimeConfiguration configuration) {
        resetSoot();
        configureSoot(configuration);
        executeSoot();
    }

    private static void resetSoot() {
        G.reset();
    }

    private static void executeSoot() {
        Scene.v().loadNecessaryClasses();
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }

    private static void configureSoot(CompileTimeConfiguration configuration) {
        Set<String> classPaths = configuration.getClassPaths().stream()
                                              .flatMap(Optimizer::getFileOrFiles)
                                              .map(Path::toString)
                                              .collect(Collectors.toSet());
        classPaths.add(System.getProperty("java.home") + "/../lib/tools.jar");

        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_include(Lists.newArrayList("jtp"));
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_asm_backend(true);
        Options.v().set_soot_classpath(String.join(":", classPaths));
        Options.v().set_process_dir(Lists.newArrayList(
                Sets.difference(
                        classPaths,
                        configuration.getExcludeClassPaths().stream()
                                     .flatMap(Optimizer::getFileOrFiles)
                                     .map(Path::toString)
                                     .collect(Collectors.toSet()))));

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.dataFlowTransform", new BodyTransformer() {
                    protected void internalTransform(Body body, String phase, Map options) {
                        // TODO: need only analyze methods that were modified during data pipe creation
                        new DataFlowAnalysis(new ExceptionalUnitGraph(body),
                                IoSinkExpressions.getAll(),
                                StringExpressionTransformer.getAll());
                    }
                }));
    }

    private static Stream<Path> getFileOrFiles(Path fileOrDirectory) {
        File[] files;

        if(fileOrDirectory.getFileName().toString().contains("*"))
            return Lists.newArrayList(fileOrDirectory.toFile()
                                                     .getParentFile()
                                                     .listFiles((FileFilter)new WildcardFileFilter(
                                                             fileOrDirectory.getFileName().toString())))
                                                     .stream().map(File::toPath);
        else if(fileOrDirectory.toFile().isDirectory() && (files = fileOrDirectory.toFile().listFiles()) != null)
            return Lists.newArrayList(files).stream().map(File::toPath);
        else
            return Lists.newArrayList(fileOrDirectory).stream();
    }
}
