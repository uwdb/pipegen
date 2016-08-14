package org.brandonhaynes.pipegen.mutation.rules;

import com.fasterxml.jackson.databind.JsonNode;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.tasks.Task;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.mutation.ExpressionReplacer;
import org.brandonhaynes.pipegen.utilities.JarUtilities;

import java.io.IOException;
import java.util.logging.Logger;

import static org.brandonhaynes.pipegen.utilities.ClassUtilities.getPipeGenDependencies;

public class ExpressionModificationRule extends ModificationRule {
    private static final Logger log = Logger.getLogger(ExpressionModificationRule.class.getName());

    private final String template;
    private final String targetExpression;
    protected final Class targetClass;

    public ExpressionModificationRule(Task task, Class sourceClass, Class targetClass) {
        this(task, sourceClass, targetClass,
             String.format("$_ = %s.intercept($$);", targetClass.getName()),
             sourceClass.getName());
    }

    public ExpressionModificationRule(Task task, Class sourceClass, Class targetClass,
                                      String template, String targetExpression) {
        super(task, sourceClass);
        this.template = template;
        this.targetClass = targetClass;
        this.targetExpression = targetExpression;
    }

    @Override
    protected boolean modifyCallSite(JsonNode node, StackFrame frame)
            throws IOException, NotFoundException, CannotCompileException {
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
        log.info(String.format("Modified expression at %s", frame.getStackFrame()));
        return true;
    }

    @Override
    protected boolean isRelevantCallSite(JsonNode node) {
        String path = getPath(node);
        //TODO
        if(path != null && !path.equals("null") && node.get("class").asText().equals(sourceClass.getName()))
            log.info("Path: " + path);
        return  node.get("class").asText().equals(sourceClass.getName()) &&
                new StackFrame(node.get("stack").get(0).asText()).getMethodName().equals("<init>") &&
                !new StackFrame(node.get("stack").get(1).asText()).getMethodName().equals("intercept") &&
                path != null && !path.contains(".class") &&
                               !path.contains(".properties") &&
                               !path.contains(".xml") &&
                               !path.contains(".so") &&
                               !path.contains(".jar") &&
                               !path.contains(".conf") &&
                               !path.contains(".log");
    }

    private static String getPath(JsonNode node) {
        String path = node.get("state").get("path") != null ? node.get("state").get("path").asText() : null;
        String pathArgument = node.get("arguments").size() == 1 ? node.get("arguments").get(0).asText() : null;

        if(path != null && !path.isEmpty() && !path.equals("null"))
            return path;
        else if(pathArgument != null && !pathArgument.isEmpty() && !pathArgument.equals("null"))
            return pathArgument;
        else
            return null;
    }
}
