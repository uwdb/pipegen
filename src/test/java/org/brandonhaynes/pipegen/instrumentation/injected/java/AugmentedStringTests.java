package org.brandonhaynes.pipegen.instrumentation.injected.java;

import org.junit.Test;

public class AugmentedStringTests {
    @Test
    public void testContainsNonNumeric() {
        AugmentedString as = new AugmentedString("foo", '\n', 2, " ", 2.5, this, new Character('$'), "3");

        assert(as.containsNonNumeric("\n"));
        assert(as.containsNonNumeric(" "));
        assert(as.containsNonNumeric("$"));
        assert(as.containsNonNumeric("3"));
        assert(!as.containsNonNumeric("@"));

        assert(!as.containsNonNumeric("2"));
        assert(!as.containsNonNumeric("2.5"));
    }
}
