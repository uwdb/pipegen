package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.Script;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.mutation.rules.datapipe.ExportRule;
import org.brandonhaynes.pipegen.runtime.proxy.ExportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

import java.io.IOException;

public class ExportTaskImpl extends BaseTask implements ExportTask {
    private ExportVerificationProxy proxy;
    private Rule rule;

    public ExportTaskImpl(CompileTimeConfiguration configuration) throws IOException {
        super(configuration, configuration.datapipeConfiguration.getExportScript());
        proxy = new ExportVerificationProxy(configuration.getBasePath());
        rule = new ExportRule(this);
    }

    public Script getExportScript() {
        return getTaskScript();
    }

    public VerificationProxy getVerificationProxy() {
        return proxy;
    }

    public Rule getRule() {
        return rule;
    }
    public Direction getDirection() { return Direction.EXPORT; }
}
