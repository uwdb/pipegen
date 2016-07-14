package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.Task;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;

import java.io.IOException;
import java.util.Collection;

public abstract class ModificationRule implements Rule {
    protected final Class sourceClass;
    protected final Task task;

    protected ModificationRule(Task task, Class sourceClass) {
        this.task = task;
        this.sourceClass = sourceClass;
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
        for(JsonNode stackFrame: node.get("stack")) {
            StackFrame frame = new StackFrame(stackFrame.asText());
            if(isRelevantStackFrame(frame))
                return isAlreadyModified(frame) ||
                        modifyCallSite(node, frame);
        }

        return false;
    }

    protected abstract boolean modifyCallSite(JsonNode node, StackFrame frame)
            throws IOException, NotFoundException, CannotCompileException;
    protected abstract boolean isRelevantCallSite(JsonNode node);

    protected boolean isRelevantStackFrame(StackFrame frame) {
        return !frame.getClassName().equals(sourceClass.getName());
    }

    protected boolean isAlreadyModified(StackFrame frame) {
        return task.getModifiedCallSites().contains(frame);
    }

    private static Collection<JsonNode> getNodes(TraceResult trace) {
        Collection<JsonNode> nodes = Lists.newArrayList();

        for(JsonNode entry: trace.getRoot())
            if(entry.findValue("state") != null)
                nodes.add(entry);
        return nodes;
    }
}
