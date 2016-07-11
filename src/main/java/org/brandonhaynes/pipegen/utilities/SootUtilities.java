package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SootUtilities {
    private static final Logger log = Logger.getLogger(SootUtilities.class.getName());

    public static Set<Class> getTransientDependencies(Class<?> clazz) {
        return getTransientDependencies(Scene.v(), Scene.v().getCallGraph(), clazz);
    }

    public static Set<Class> getTransientDependencies(Scene scene, CallGraph callGraph, Class<?> clazz) {
        Queue<Class> pendingClasses = ArrayUtilities.newArrayDeque(clazz);
        Set<Class> classes = Sets.newHashSet();

        while(!pendingClasses.isEmpty()) {
            Set<Class> newClasses = getDependencies(scene, callGraph, pendingClasses.poll())
                                        .stream()
                                        .filter(c -> !classes.contains(c))
                                        .collect(Collectors.toSet());
            classes.addAll(newClasses);
            pendingClasses.addAll(newClasses);
        }

        return classes;
    }

    public static Set<Class> getDependencies(Scene scene, CallGraph callGraph, Class<?> clazz) {
        Set<Class> dependencies = Sets.newHashSet();

        SootClass sootClass = scene.getSootClass(clazz.getName());

        for(SootMethod method: sootClass.getMethods())
            if(!method.isPhantom())
                callGraph.edgesInto(method).forEachRemaining(edge -> {
                    try {
                        dependencies.add(Class.forName(edge.getSrc().method().getDeclaringClass().getName()));
                    } catch (ClassNotFoundException e) {
                        log.info(String.format("Class not found: %s", edge.getSrc().method().getDeclaringClass().getName()));
                    }
                });

        return dependencies;
    }

    public static CallGraph getCallGraph(String entryPoint) {
        Scene scene = initializeSoot(ArrayUtilities.newArrayList(Paths.get("/home/bhaynes/research/pipegen/lib/*"),
                                                                 Paths.get("/home/bhaynes/research/pipegen/target/*")),
                                     entryPoint); //TODO generalize
        PackManager.v().runPacks();
        PackManager.v().writeOutput();

        return scene.getCallGraph();
    }

    public static Scene initializeSoot(CompileTimeConfiguration configuration, String entryPoint) {
        return initializeSoot(configuration.getClassPaths(), configuration.getExcludeClassPaths(), entryPoint);
    }

    public static Scene initializeSoot(Collection<Path> classPaths, String entryPoint) {
        return initializeSoot(classPaths, Lists.newArrayList(), entryPoint);
    }

    public static Scene initializeSoot(Collection<Path> classPaths, Collection<Path> excludedClassPaths,
                                       String entryPoint) {
        resetSoot();
        configureSoot(classPaths, excludedClassPaths, entryPoint);
        Scene.v().loadNecessaryClasses();

        return Scene.v();
    }

    private static void resetSoot() {
        G.reset();
    }

    public static void configureSoot(CompileTimeConfiguration configuration, String entryPoint) {
        configureSoot(configuration.getClassPaths(), configuration.getExcludeClassPaths(), entryPoint);
    }

    public static void configureSoot(Collection<Path> classPaths, String entryPoint) {
        configureSoot(classPaths, Lists.newArrayList(), entryPoint);
    }

    private static void configureSoot(Collection<Path> classPaths, Collection<Path> excludedClassPaths,
                                      String entryPoint) {
        Set<String> expandedClassPaths = classPaths.stream()
                                                   .flatMap(SootUtilities::getFileOrFiles)
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

        Options.v().set_soot_classpath(String.join(":", expandedClassPaths));

        Options.v().set_process_dir(Lists.newArrayList(
                Sets.difference(
                        getDistinctPaths(expandedClassPaths),
                        excludedClassPaths.stream()
                                          .flatMap(SootUtilities::getFileOrFiles)
                                          .map(Path::toString)
                                          .collect(Collectors.toSet()))));
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
            return Lists.newArrayList(new Path[] {fileOrDirectory}).stream();
    }

    private static Set<String> getDistinctPaths(Set<String> filesOrDirectories) {
        return filesOrDirectories.stream()
                .map(File::new)
                .map(f -> f.isDirectory() ? f : f.getParent())
                .filter(f -> f != null)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
