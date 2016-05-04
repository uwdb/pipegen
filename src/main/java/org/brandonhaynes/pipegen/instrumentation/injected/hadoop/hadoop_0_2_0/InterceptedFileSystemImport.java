package org.brandonhaynes.pipegen.instrumentation.injected.hadoop.hadoop_0_2_0;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.injected.filesystem.InterceptedFileInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class InterceptedFileSystemImport extends FSDataInputStream {
	public static FSDataInputStream intercept(org.apache.hadoop.fs.FileSystem fs, org.apache.hadoop.fs.Path path) throws IOException {
		return RuntimeConfiguration.getInstance().getFilenamePattern().matcher(path.getName()).matches()
				? new InterceptedFileSystemImport(path)
				: fs.open(path);
	}

	public static Collection<Class> getDependencies() {
		return new ArrayList<Class>() {{
			add(InterceptedFileSystemImport.class);
            add(RuntimeConfiguration.class);
            add(SeekableInputStream.class);
			addAll(InterceptedFileInputStream.getDependencies());
		}};
	}

	private InterceptedFileSystemImport(Path path) throws IOException {
        super(new SeekableInputStream(new InterceptedFileInputStream(path.getName()), 4096));
	}
}
