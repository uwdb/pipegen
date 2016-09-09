package org.brandonhaynes.pipegen.instrumentation.injected.utility;

import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.nio.file.Paths;
import java.util.regex.Matcher;

public class InterceptUtilities {
    public static String getSystemName(String filename, Direction direction) {
        Matcher matcher = RuntimeConfiguration.getInstance().getFilenamePattern(direction).matcher(filename);
        if(!matcher.find())
            throw new IllegalArgumentException(String.format("Could not identify system name in filename %s", filename));
        else if(RuntimeConfiguration.getInstance().isInVerificationMode(direction))
            return Paths.get(matcher.group("name").replaceFirst("^file:", "")).toAbsolutePath().toString();
        else
            return matcher.group("name");
    }
}
