package org.brandonhaynes.pipegen.configuration;

import org.brandonhaynes.pipegen.instrumentation.StackFrame;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

import java.util.Set;

public interface Task {
    Script getTaskScript();
    CompileTimeConfiguration getConfiguration();
    Set<StackFrame> getModifiedCallSites();
    VerificationProxy getVerificationProxy();
    Rule getRule();
}
