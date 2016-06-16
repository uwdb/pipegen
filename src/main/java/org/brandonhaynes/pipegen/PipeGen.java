package org.brandonhaynes.pipegen;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.instrumentation.InstrumentationListener;
import org.brandonhaynes.pipegen.runtime.directory.VerificationWorkerDirectory;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryServer;
import org.brandonhaynes.pipegen.runtime.proxy.ImportVerificationProxy;
import org.brandonhaynes.pipegen.utilities.DataPipeTasks;
import org.brandonhaynes.pipegen.utilities.HostListener;
import sun.jvmstat.monitor.MonitorException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PipeGen {
    private static final Logger log = Logger.getLogger(PipeGen.class.getName());

    public static void main(String[] args)
            throws IOException, InterruptedException, MonitorException{
        if(args.length != 1)
            Usage(PipeGen.class.getName(), System.out);
        else
            main(args[0]);
    }

    private static void main(String configurationFilename)
            throws IOException, InterruptedException, MonitorException{
        main(Paths.get(configurationFilename));
    }

    private static void main(Path configurationFile)
            throws IOException, InterruptedException, MonitorException{
        main(new CompileTimeConfiguration(configurationFile));
    }

    private static void main(CompileTimeConfiguration configuration)
            throws IOException, InterruptedException, MonitorException {
        DataPipeTasks.create(configuration);
    }

    private static void Usage(String name, PrintStream writer) {
        writer.print(String.format("%s: [configuration filename]\n", name));
    }
}
