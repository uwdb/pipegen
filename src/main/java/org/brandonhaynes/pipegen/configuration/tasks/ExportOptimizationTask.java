package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.ExportRule;
import org.brandonhaynes.pipegen.runtime.proxy.ExportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

import java.io.IOException;

public class ExportOptimizationTask extends BaseTask implements OptimizationTask {
    private ExportVerificationProxy proxy;
    private Rule rule;

    public ExportOptimizationTask(CompileTimeConfiguration configuration) {
        super(configuration, configuration.datapipeConfiguration.getExportScript());

        try {
            proxy = new ExportVerificationProxy(configuration.getBasePath());
            rule = new ExportRule(this);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VerificationProxy getVerificationProxy() {
        return proxy;
    }

    public Rule getRule() {
        return rule;
    }
    public Direction getDirection() { return Direction.EXPORT; }
}
