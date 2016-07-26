package org.brandonhaynes.pipegen.configuration.tasks;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.Script;
import org.brandonhaynes.pipegen.mutation.rules.Rule;
import org.brandonhaynes.pipegen.mutation.rules.datapipe.ImportRule;
import org.brandonhaynes.pipegen.runtime.proxy.ImportVerificationProxy;
import org.brandonhaynes.pipegen.runtime.proxy.VerificationProxy;

public class ImportTaskImpl extends BaseTask implements ImportTask {
    private ImportVerificationProxy proxy;
    private Rule rule;

    public ImportTaskImpl(CompileTimeConfiguration configuration) {
        super(configuration, configuration.datapipeConfiguration.getImportScript());
        proxy = new ImportVerificationProxy(configuration.getBasePath());
        rule = new ImportRule(this);
    }

    public Script getImportScript() {
        return getTaskScript();
    }

    public VerificationProxy getVerificationProxy() {
        return proxy;
    }

    public Rule getRule() {
        return rule;
    }
    public Direction getDirection() { return Direction.IMPORT; }
}
