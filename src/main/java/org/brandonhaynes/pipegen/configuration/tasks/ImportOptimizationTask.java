package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.mutation.rules.optimization.ImportRule;
import org.brandonhaynes.pipegen.runtime.proxy.ImportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

public class ImportOptimizationTask extends BaseTask implements OptimizationTask {
    private ImportVerificationProxy proxy;
    private Rule rule;

    public ImportOptimizationTask(CompileTimeConfiguration configuration) {
        super(configuration, configuration.datapipeConfiguration.getExportScript());
        proxy = new ImportVerificationProxy(configuration.getBasePath());
        rule = new ImportRule(this);
    }

    public VerificationProxy getVerificationProxy() {
        return proxy;
    }

    public Rule getRule() {
        return rule;
    }
    public Direction getDirection() { return Direction.IMPORT; }
}
