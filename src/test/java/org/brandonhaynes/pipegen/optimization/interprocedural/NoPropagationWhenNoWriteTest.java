package org.brandonhaynes.pipegen.optimization.interprocedural;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class NoPropagationWhenNoWriteTest extends OptimizationTest  {
    @Override
    protected String getDataflowMethodSignature() { return "child(java.lang.String)"; }

    @Override
    public void test() throws IOException {
        Integer boxed = 5;
        String converted = boxed.toString();
        child(converted);
    }

    private void child(String converted) throws IOException {
        OutputStreamWriter writer = getTestWriter();

        assert(!(converted instanceof AugmentedString));
    }
}