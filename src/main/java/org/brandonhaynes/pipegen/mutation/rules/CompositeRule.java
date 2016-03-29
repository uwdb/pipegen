package org.brandonhaynes.pipegen.mutation.rules;

import com.google.common.collect.Iterables;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.instrumentation.TraceResult;

import java.io.IOException;
import java.util.Arrays;

public class CompositeRule implements Rule {
    private final Iterable<Rule> rules;

    public CompositeRule(Rule... rules) {
        this.rules = Arrays.asList(rules);
    }

    public boolean isApplicable(TraceResult trace) {
        return Iterables.any(rules, rule -> rule.isApplicable(trace));
    }

    public boolean apply(TraceResult trace) throws IOException, NotFoundException, CannotCompileException {
        boolean result = true;

        for(Rule rule: rules)
            result &= rule.apply(trace);

        return result;
    }

}
