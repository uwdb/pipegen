package org.brandonhaynes.pipegen.optimization.intraprocedural;

import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ConditionalBoxedWriterTest extends OptimizationTest  {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        Integer boxed = 5;
        String converted = boxed.toString();

        if(boxed >= 5)
            writer.write(converted);
    }
}