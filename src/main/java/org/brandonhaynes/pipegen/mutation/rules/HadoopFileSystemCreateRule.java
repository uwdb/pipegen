package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.brandonhaynes.pipegen.configuration.ExportTask;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;
import org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_0_2_0.InterceptedFileSystemExport;
import org.brandonhaynes.pipegen.mutation.ExpressionReplacer;
import org.brandonhaynes.pipegen.utilities.JarUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;

import static org.brandonhaynes.pipegen.utilities.ClassUtilities.getPipeGenDependencies;

public class HadoopFileSystemCreateRule implements Rule {
    private static final Logger log = Logger.getLogger(HadoopFileSystemCreateRule.class.getName());

    private static final Collection<String> sourceClasses = Lists.newArrayList(
            FileSystem.class.getName(), LocalFileSystem.class.getName(),
            DistributedFileSystem.class.getName(), RawLocalFileSystem.class.getName());
    private static final Class targetClass = InterceptedFileSystemExport.class;
    private static final String template = String.format("$_ = %s.intercept($0, $$);", targetClass.getName());
    private static final String targetExpression = "create";

    private final ExportTask task;

    public HadoopFileSystemCreateRule(ExportTask task) {
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
        for (URL url : task.getConfiguration().findClasses(frame.getClassName())) {
            log.info(String.format("Modifying call sites in %s", url));
            JarUtilities.replaceClasses(
                    url,
                    task.getConfiguration().getClassPool(),
                    getPipeGenDependencies(),
                    task.getConfiguration().getVersion(),
                    task.getConfiguration().getBackupPath());

            ExpressionReplacer.replaceExpression(
                    url, frame.getClassName(), frame.getMethodName(), frame.getLine().get(),
                    targetExpression, template, task.getConfiguration().getBackupPath());
        }

        task.getModifiedCallSites().add(frame);
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
        JsonNode uriNode = node.get("state").get("uri");
        String uri = uriNode != null ? uriNode.asText() : null;
        return uri != null && !uri.isEmpty() && !uri.equals("null")
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
