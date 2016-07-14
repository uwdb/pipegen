package org.brandonhaynes.pipegen.utilities;

import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.configuration.tasks.OptimizationTask;
import org.brandonhaynes.pipegen.configuration.tasks.Task;
import org.brandonhaynes.pipegen.instrumentation.InstrumentationListener;
import org.brandonhaynes.pipegen.runtime.directory.VerificationWorkerDirectory;
import org.brandonhaynes.pipegen.runtime.directory.WorkerDirectoryServer;
import sun.jvmstat.monitor.MonitorException;

import java.io.IOException;
import java.util.logging.Logger;

public class DataPipeTasks {
    private static final Logger log = Logger.getLogger(DataPipeTasks.class.getName());

    public static void create(CompileTimeConfiguration configuration)
            throws IOException, InterruptedException, MonitorException{
        //create(configuration.importTask);
        //create(configuration.exportTask);
        optimize(configuration.exportOptimizationTask);
    }

    public static void create(Task task) throws IOException, InterruptedException, MonitorException {
        if(!instrument(task) ||
           //!verifyExistingFunctionality(task) ||
           !verifyDataPipeFunctionality(task))
            rollback(task.getConfiguration());
        log.info("Done");
    }

    private static void optimize(OptimizationTask task)
            throws IOException, MonitorException, InterruptedException {
        HostListener listener = new InstrumentationListener(task);
        verifyDataPipeFunctionality(task);
        listener.join();
    }

    private static boolean instrument(Task task) throws IOException, MonitorException, InterruptedException {
        HostListener listener = new InstrumentationListener(task);
        Process process = test(task);
        listener.join();
        process.destroy();
        return process.exitValue() == 0;
    }

    public static Process test(Task task) throws IOException {
        log.info(String.format("Testing %s (%s)", task.getConfiguration().getSystemName(), task.getTaskScript()));

        return task.getTaskScript().getProcessBuilder().start();
    }

    private static boolean verifyExistingFunctionality(Task task) throws IOException, InterruptedException {
        return verify(task.getConfiguration(), true, false) == 0;
    }

    private static boolean verifyDataPipeFunctionality(Task task) throws IOException, InterruptedException {
        WorkerDirectoryServer directory = WorkerDirectoryServer.startIfNotStarted(
                new VerificationWorkerDirectory(),
                RuntimeConfiguration.getInstance().getWorkerDirectoryUri().getPort(),
                task.getConfiguration().datapipeConfiguration.getLogPropertiesPath());

        task.getVerificationProxy().start();
        int exitValue = verify(task.getConfiguration(), false, false);
        task.getVerificationProxy().stop();

        if(directory != null)
            directory.stop(true);

        return exitValue == 0;
    }

    private static int verify(CompileTimeConfiguration configuration, boolean isVerifyingExistingFunctionality,
                                                                      boolean isTransferOptimized)
            throws IOException, InterruptedException {
        log.info(String.format("Verifying %s (%s)", configuration.getSystemName(),
                isVerifyingExistingFunctionality ? "existing functionality" : "datapipe functionality"));

        ProcessBuilder builder = configuration.datapipeConfiguration.getVerifyScript().getProcessBuilder();
        RuntimeConfiguration.setProcessVerificationMode(builder, !isVerifyingExistingFunctionality);
        RuntimeConfiguration.setProcessOptimizationMode(builder, !isVerifyingExistingFunctionality && isTransferOptimized);
        Process process = builder.start();
        process.waitFor();

        log.info(String.format("Verification %s (%d)", process.exitValue() == 0 ? "complete" : "failed", process.exitValue()));
        return process.exitValue();
    }

    public static void rollback(CompileTimeConfiguration configuration) throws IOException {
        log.info(String.format("Rolling back %s", configuration.getSystemName()));
        FileRestorer.restoreFiles(configuration.getBackupPath(), new String[] {"jar", "class"});
    }
}
