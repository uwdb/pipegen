package org.brandonhaynes.pipegen.mutation.rules.datapipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.ExportTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileOutputStream;
import org.brandonhaynes.pipegen.mutation.ExpressionReplacer;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.utilities.JarUtilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static org.brandonhaynes.pipegen.utilities.ClassUtilities.getPipeGenDependencies;

public class FileOutputStreamRule implements Rule {
    private static final Logger log = Logger.getLogger(FileOutputStreamRule.class.getName());

    private static final Class sourceClass = FileOutputStream.class;
    private static final Class targetClass = InterceptedFileOutputStream.class;
    private static final String template = String.format("$_ = %s.intercept($$);", targetClass.getName());
    private static final String targetExpression = ".*";

    private final ExportTask task;


    public FileOutputStreamRule(ExportTask task) {
        this.task = task;
    }

    public boolean isApplicable(TraceResult trace) {
        return !getNodes(trace).isEmpty();
    }

    public boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException {
        boolean result = false;

        for(JsonNode node: getNodes(trace))
            if(isRelevantCallSite(node))
                result |= apply(node);

        return result;
    }

    public boolean apply(JsonNode node) throws IOException, NotFoundException, CannotCompileException {
        // Modify the first stack frame that instantiates FileOutputStream
        for(JsonNode stackFrame: node.get("stack")) {
            StackFrame frame = new StackFrame(stackFrame.asText());
            if(isRelevantStackFrame(frame))
                return isAlreadyModified(frame) ||
                        modifyCallSite(frame);
        }

        return false;
    }

    private boolean modifyCallSite(StackFrame frame) throws IOException, NotFoundException, CannotCompileException {
        ExpressionReplacer.replaceExpression(
                frame.getClassName(), frame.getMethodName(), frame.getLine().get(),
                targetExpression, template, task.getConfiguration().instrumentationConfiguration.getClassPool(),
                task.getConfiguration().getBackupPath());
        JarUtilities.replaceClasses(task.getConfiguration().instrumentationConfiguration.getClassPool().find(frame.getClassName()),
                task.getConfiguration().instrumentationConfiguration.getClassPool(),
                getPipeGenDependencies(),
                task.getConfiguration().getVersion(),
                task.getConfiguration().getBackupPath());
        task.getModifiedCallSites().add(frame);
        log.info(String.format("Injected data pipe at %s", frame.getStackFrame()));
        return true;
    }

    private boolean isRelevantStackFrame(StackFrame frame) {
        return !frame.getClassName().equals(sourceClass.getName());
    }

    private boolean isAlreadyModified(StackFrame frame) {
        return task.getModifiedCallSites().contains(frame);
    }

    private boolean isRelevantCallSite(JsonNode node) {
        String path = getPath(node);
        //TODO
        if(path != null && !path.equals("null") && node.get("class").asText().equals(sourceClass.getName()))
            log.info("Path: " + path);
        return  node.get("class").asText().equals(sourceClass.getName()) &&
                new StackFrame(node.get("stack").get(0).asText()).getMethodName().equals("<init>") &&
                path != null && !path.contains(".class") &&
                               !path.contains(".properties") &&
                               !path.contains(".xml") &&
                               !path.contains(".so") &&
                               !path.contains(".jar") &&
                               !path.contains(".out") &&
                               !path.contains(".log");
    }

    private String getPath(JsonNode node) {
        String path = node.get("state").get("path") != null ? node.get("state").get("path").asText() : null;
        String pathArgument = node.get("arguments").size() == 1 ? node.get("arguments").get(0).asText() : null;

        if(path != null && !path.isEmpty() && !path.equals("null"))
            return path;
        else if(pathArgument != null && !pathArgument.isEmpty() && !pathArgument.equals("null"))
            return pathArgument;
        else
            return null;
    }

    private Collection<JsonNode> getNodes(TraceResult trace) {
        Collection<JsonNode> nodes = Lists.newArrayList();

        for(JsonNode entry: trace.getNodes())
            if(entry.findValue("state") != null)
                nodes.add(entry);
        return nodes;
    }
}
