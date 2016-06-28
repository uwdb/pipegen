package org.brandonhaynes.pipegen.optimization.interprocedural;

import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ShallowChildBoxedWriterTest extends OptimizationTest  {
    @Override
    protected String getDataflowMethodSignature() { return "grandchild(java.lang.String)"; }

    @Override
    public void test() throws IOException {
        child(5);
    }

    private void child(Integer boxed) throws IOException {
        String converted = boxed.toString();
        grandchild(converted);
    }

    private void grandchild(String converted) throws IOException {
        OutputStreamWriter writer = getTestWriter();

        if(converted != null)
            writer.write(converted);
    }
}