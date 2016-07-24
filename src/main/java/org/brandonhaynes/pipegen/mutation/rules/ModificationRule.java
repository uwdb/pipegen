package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.Task;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;

import java.io.IOException;
import java.util.stream.StreamSupport;

public abstract class ModificationRule implements Rule {
    protected final Class sourceClass;
    protected final Task task;

    protected ModificationRule(Task task, Class sourceClass) {
        this.task = task;
        this.sourceClass = sourceClass;
    }

    public boolean isApplicable(TraceResult trace) {
        return !getNodesWithState(trace).iterator().hasNext();
    }

    public boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException {
        boolean result = false;

        for(JsonNode node: getNodesWithState(trace))
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

    protected abstract boolean modifyCallSite(JsonNode node, StackFrame frame)
            throws IOException, NotFoundException, CannotCompileException;
    protected abstract boolean isRelevantCallSite(JsonNode node);

    protected boolean isRelevantStackFrame(StackFrame frame) {
        return !frame.getClassName().equals(sourceClass.getName());
    }

    protected boolean isAlreadyModified(StackFrame frame) {
        return task.getModifiedCallSites().contains(frame);
    }

    private static Iterable<JsonNode> getNodesWithState(TraceResult trace) {
        return StreamSupport.stream(trace.getNodes().spliterator(), false)
                            .filter(node -> node.findValue("state") != null)::iterator;
        //return trace.getNodes()
        //Collection<JsonNode> nodes = Lists.newArrayList();

        //for(JsonNode entry: trace.getNodes())
        //    if(entry.findValue("state") != null)
        //        nodes.add(entry);
        //return nodes;
    }
}
