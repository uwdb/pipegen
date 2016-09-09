package org.brandonhaynes.pipegen.mutation.rules.datapipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.apache.hadoop.fs.FileSystem;
import org.brandonhaynes.pipegen.configuration.tasks.ImportTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;
import org.brandonhaynes.pipegen.instrumentation.injected.hadoop.InterceptedFSDataInputStream;
import org.brandonhaynes.pipegen.mutation.ExpressionReplacer;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.utilities.JarUtilities;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static org.brandonhaynes.pipegen.utilities.ClassUtilities.getPipeGenDependencies;

public class FileSystemOpenRule implements Rule {
    private static final Logger log = Logger.getLogger(FileInputStreamRule.class.getName());

    private static final Class sourceClass = FileSystem.class;
    private static final Class targetClass = InterceptedFSDataInputStream.class;
    private static final String targetMethodName = "open";
    private static final String template = String.format("$_ = %s.intercept($0, $$);", targetClass.getName());

    private final ImportTask task;

    public FileSystemOpenRule(ImportTask task) {
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
        for(JsonNode stackFrame: node.get("stack")) {
            StackFrame frame = new StackFrame(stackFrame.asText());
            if(isRelevantStackFrame(frame))
                return isAlreadyModified(frame) ||
                        modifyCallSite(node, frame);
        }

        return false;
    }

    //TODO should be idempotent and refuse to modify any call sites inside InterceptedFileInputStream.intercept(...)
    private boolean modifyCallSite(JsonNode node, StackFrame frame)
            throws IOException, NotFoundException, CannotCompileException {
        String targetExpression = new StackFrame(node.get("stack").get(0).asText()).getClassName() + "." + targetMethodName;
        ExpressionReplacer.replaceExpression(
                frame.getClassName(), frame.getMethodName(), frame.getLine(),
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
        return !frame.getClassName().startsWith(sourceClass.getPackage().getName()) ||
               !frame.getClassName().contains(sourceClass.getName());
    }

    private boolean isAlreadyModified(StackFrame frame) {
        return task.getModifiedCallSites().contains(frame);
    }

    private boolean isRelevantCallSite(JsonNode node) {
        return node.get("class").textValue().contains(sourceClass.getSimpleName()) &&
               node.get("class").textValue().contains(sourceClass.getPackage().getName());
    }

    private Collection<JsonNode> getNodes(TraceResult trace) {
        Collection<JsonNode> nodes = Lists.newArrayList();

        for(JsonNode entry: trace.getNodes())
            if(entry.findValue("state") != null)
                nodes.add(entry);
        return nodes;
    }
}
