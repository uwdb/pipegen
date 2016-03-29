package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.brandonhaynes.pipegen.configuration.ImportTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;
import org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_0_2_0.InterceptedFileSystem;
import org.brandonhaynes.pipegen.mutation.ExpressionReplacer;
import org.brandonhaynes.pipegen.utilities.JarUpdater;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

public class HadoopFileSystemOpenRule implements Rule {
    private static final Logger log = Logger.getLogger(HadoopFileSystemOpenRule.class.getName());

    private static final Collection<String> sourceClasses = Lists.newArrayList(
            FileSystem.class.getName(), LocalFileSystem.class.getName(),
            DistributedFileSystem.class.getName(), RawLocalFileSystem.class.getName());
    private static final Class targetClass = InterceptedFileSystem.class;
    private static final String template = String.format("$_ = %s.intercept($$);", targetClass.getName());

    private final ImportTask task;

    public HadoopFileSystemOpenRule(ImportTask task) {
        this.task = task;
    }

    public boolean isApplicable(TraceResult trace) {
        return !getNodes(trace).isEmpty();
    }

    public boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException {
        boolean result = true;

        for(JsonNode node: getNodes(trace))
            if(isRelevantCallSite(node))
                result &= apply(node);

        return result;
    }

    public boolean apply(JsonNode node) throws IOException, NotFoundException, CannotCompileException {
        // Modify the first stack frame that instantiates FileInputStream
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
                template, task.getConfiguration().getClassPool());
        JarUpdater.replaceClasses(task.getConfiguration().getClassPool().find(frame.getClassName()),
                task.getConfiguration().getClassPool(),
                InterceptedFileSystem.getDependencies(),
                task.getConfiguration().getVersion());
        task.getModifiedCallSites().add(frame);
        log.info(String.format("Injected data pipe at %s", frame.getStackFrame()));
        return true;
    }

    private boolean isRelevantStackFrame(StackFrame frame) {
        return !sourceClasses.contains(frame.getClassName());
    }

    private boolean isAlreadyModified(StackFrame frame) {
        return task.getModifiedCallSites().contains(frame);
    }

    private boolean isRelevantCallSite(JsonNode node) {
        String uri = getUri(node);
        //TODO
        return sourceClasses.contains(node.get("class").asText()) &&
                uri != null && !uri.contains(".class") && !uri.contains(".properties");
    }

    private String getUri(JsonNode node) {
        String uri = node.get("state").get("uri").asText();
        return !uri.isEmpty() && !uri.equals("null")
                ? uri
                : null;
    }

    private Collection<JsonNode> getNodes(TraceResult trace) {
        Collection<JsonNode> nodes = Lists.newArrayList();

        for(JsonNode entry: trace.getRoot())
            if(entry.findValue("state") != null)
                nodes.add(entry);
        return nodes;
    }
}
