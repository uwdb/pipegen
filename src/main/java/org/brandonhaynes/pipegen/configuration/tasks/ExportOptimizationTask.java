package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.ExportRule;
import org.brandonhaynes.pipegen.runtime.proxy.ExportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

import java.io.IOException;

public class ExportOptimizationTask extends BaseTask implements OptimizationTask {
    private ExportVerificationProxy proxy;
    private Rule rule;

    public ExportOptimizationTask(CompileTimeConfiguration configuration) throws IOException {
        super(configuration, configuration.datapipeConfiguration.getExportScript());
        proxy = new ExportVerificationProxy(configuration.getBasePath());
        rule = new ExportRule(this);
    }

    public VerificationProxy getVerificationProxy() {
        return proxy;
    }

    public Rule getRule() {
        return rule;
    }
}