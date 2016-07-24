package org.brandonhaynes.pipegen.instrumentation;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.Task;
import org.brandonhaynes.pipegen.mutation.rules.CompositeRule;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.utilities.HostListener;
import sun.jvmstat.monitor.MonitorException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;

public class InstrumentationListener extends HostListener {
    public InstrumentationListener(Task task) throws MonitorException {
        this(task.getConfiguration().instrumentationConfiguration.getPort(),
             task.getConfiguration().instrumentationConfiguration.getClassPattern(),
             task.getConfiguration().instrumentationConfiguration.getCommandPattern(),
             task.getConfiguration().instrumentationConfiguration.getTraceFile(),
             task.getConfiguration().instrumentationConfiguration.getClassPaths(),
             task.getConfiguration().instrumentationConfiguration.getAgentFile(),
             task.getConfiguration().instrumentationConfiguration.getTimeout(),
             task.getConfiguration().instrumentationConfiguration.isDebug(),
             task.getRule());
    }

    public InstrumentationListener(int clientPort, Pattern classNamePattern, Pattern commandLinePattern,
                                   Path traceFilename, Collection<Path> classPaths, Path agentFile,
                                   int timeout, boolean debug,
                                   Rule... rules)
            throws MonitorException {
        super((metadata) -> classNamePattern.matcher(metadata.getLeft()).matches() &&
                            commandLinePattern.matcher(metadata.getRight()).matches(),
              (processId) -> instrumentProcess(
                      processId, clientPort, traceFilename, classPaths, agentFile, timeout, debug, rules), timeout);
    }

    private static boolean instrumentProcess(int processId, int clientPort, Path traceFile,
                                             Collection<Path> classPaths, Path agentFile, int timeout, boolean debug,
                                             Rule... rules) {
        return instrumentProcess(processId, clientPort, traceFile, classPaths, agentFile,
                                 timeout, debug, new CompositeRule(rules));
    }

    private static boolean instrumentProcess(int processId, int clientPort, Path traceFile, Collection<Path> classPaths,
                                             Path agentFile, int timeout, boolean debug, Rule rule) {
        try {
            TraceResult trace = OperationTracer.traceOperation(processId, clientPort, traceFile,
                    classPaths, agentFile, timeout, debug);
            return rule.isApplicable(trace) && rule.apply(trace);
        } catch(IOException | NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }
}
