package org.brandonhaynes.pipegen.optimization.augmentation;

import org.brandonhaynes.pipegen.instrumentation.injected.java.AugmentedString;
import org.brandonhaynes.pipegen.optimization.OptimizationTest;
import org.brandonhaynes.pipegen.support.MockResultSet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetTest extends OptimizationTest  {
    String s2;

    @Override
    public void test() throws IOException {
        OutputStreamWriter writer = getTestWriter();

        try {
            ResultSet resultSet = new MockResultSet(999);
            String s = resultSet.getString(0);

            assert (s instanceof AugmentedString);
            assert (s.equals("999"));
            assert (((AugmentedString)s).getState()[0].equals(999));

            s2 = resultSet.getString("column");

            assert (s2 instanceof AugmentedString);
            assert (s2.equals("999"));
            assert (((AugmentedString)s).getState()[0].equals(999));

            writer.write(s);
            writer.write(s2);
        } catch(SQLException e) {
            throw new IOException(e);
        }
    }
}