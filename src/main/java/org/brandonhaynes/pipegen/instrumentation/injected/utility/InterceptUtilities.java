package org.brandonhaynes.pipegen.instrumentation.injected.utility;

import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.util.regex.Matcher;

public class InterceptUtilities {
    public static String getSystemName(String filename) {
        Matcher matcher = RuntimeConfiguration.getInstance().getFilenamePattern().matcher(filename);
        if(!matcher.find())
            throw new IllegalArgumentException(String.format("Could not identify system name in filename %s", filename));
        return matcher.group("name");
    }
}
