package org.brandonhaynes.pipegen.instrumentation.injected.hadoop;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.brandonhaynes.pipegen.configuration.Direction;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class InterceptedTextInputFormat extends TextInputFormat {
    public static Object intercept(Constructor constructor, Object[] parameters)
            throws InstantiationException, InvocationTargetException, IllegalAccessException {
        if(TextInputFormat.class.isAssignableFrom(constructor.getDeclaringClass())) {
            assert(parameters.length == 0);
            return new InterceptedTextInputFormat();
        } else
            return constructor.newInstance(parameters);
    }

    @Override
    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        FileStatus[] files = this.listStatus(job);

        return files.length == 1 &&
               RuntimeConfiguration.getInstance().getFilenamePattern(Direction.IMPORT)
                                                 .matcher(files[0].getPath().toString()).matches()
            ? new InputSplit[] { makeSplit(files[0].getPath(), 0, Integer.MAX_VALUE, new String[] {})}
            : super.getSplits(job, numSplits);
    }
}