package org.brandonhaynes.pipegen.configuration.tasks;

import com.google.common.collect.Sets;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.Script;
import org.brandonhaynes.pipegen.instrumentation.StackFrame;

import java.util.Set;

public abstract class BaseTask implements Task {
    private final CompileTimeConfiguration configuration;
    private final Script script;
    private final Set<StackFrame> modifiedCallSites = Sets.newHashSet();

    public BaseTask(CompileTimeConfiguration configuration, Script script) {
        this.configuration = configuration;
        this.script = script;
    }

    public CompileTimeConfiguration getConfiguration() {
        return configuration;
    }

    public Script getTaskScript() {
        return script;
    }

    public Set<StackFrame> getModifiedCallSites() {
        return modifiedCallSites;
    }
}
