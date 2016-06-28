package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedStringBuilder;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class StringBuilderTest extends OptimizationTest  {
    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();
        StringBuilder builder = new StringBuilder();

        builder.append("a");
        builder.append("bc");
        assert(builder instanceof AugmentedStringBuilder);
        assert(builder.toString() instanceof AugmentedString);
        assert(builder.toString().equals("abc"));

        writer.write(builder.toString());
    }
}