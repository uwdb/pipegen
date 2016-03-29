package org.brandonhaynes.pipegen.utilities;

import com.google.common.base.Joiner;
import org.brandonhaynes.pipegen.configuration.CompileTimeConfiguration;
import org.brandonhaynes.pipegen.configuration.RuntimeConfiguration;
import org.brandonhaynes.pipegen.configuration.Task;
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
        create(configuration.importTask);
        create(configuration.exportTask);
    }

    public static void create(Task task) throws IOException, InterruptedException, MonitorException {
        DataPipeTasks.build(task.getConfiguration());
        DataPipeTasks.instrument(task);
        DataPipeTasks.verifyExistingFunctionality(task);
        DataPipeTasks.verifyDataPipeFunctionality(task);
    }

    public static boolean build(CompileTimeConfiguration configuration) throws IOException, InterruptedException {
        log.info(String.format("Building %s (%s)", configuration.getSystemName(), configuration.datapipeConfiguration.getBuildScript()));

        ProcessBuilder builder = getBuilder(configuration.datapipeConfiguration.getBuildScript(), configuration);
        Process process = builder.start();
        process.waitFor();

        log.info(String.format("Build %s (%d)", process.exitValue() == 0 ? "complete" : "failed", process.exitValue()));
        return process.exitValue() == 0;
    }

    public static boolean instrument(Task task)
            throws IOException, MonitorException, InterruptedException {
        HostListener listener = new InstrumentationListener(task);
        Process process = DataPipeTasks.test(task);
        listener.join();
        process.destroy();
        return process.exitValue() == 0;
    }

    public static Process test(Task task) throws IOException {
        log.info(String.format("Testing %s (%s)", task.getConfiguration().getSystemName(), task.getTaskScript()));

        ProcessBuilder builder = getBuilder(task.getTaskScript(), task);
        return builder.start();
    }

    public static boolean verifyExistingFunctionality(Task task) throws IOException, InterruptedException {
        return verify(task.getConfiguration(), true) == 0;
    }

    public static boolean verifyDataPipeFunctionality(Task task) throws IOException, InterruptedException {
        WorkerDirectoryServer directory = WorkerDirectoryServer.startIfNotStarted(
                new VerificationWorkerDirectory(),
                RuntimeConfiguration.getInstance().getWorkerDirectoryUri().getPort(),
                task.getConfiguration().datapipeConfiguration.getLogPropertiesPath());

        task.getVerificationProxy().start();
        int exitValue = verify(task.getConfiguration(), false);
        task.getVerificationProxy().stop();

        if(directory != null)
            directory.stop();

        return exitValue == 0;
    }

    private static int verify(CompileTimeConfiguration configuration, boolean isVerifyingExistingFunctionality)
            throws IOException, InterruptedException {
        log.info(String.format("Verifying %s (%s)", configuration.getSystemName(),
                isVerifyingExistingFunctionality ? "existing functionality" : "datapipe functionality"));

        ProcessBuilder builder = getBuilder(configuration.datapipeConfiguration.getVerifyScript(), configuration);
        RuntimeConfiguration.setProcessVerificationMode(builder, !isVerifyingExistingFunctionality);
        Process process = builder.start();
        process.waitFor();

        log.info(String.format("Verification %s (%d)", process.exitValue() == 0 ? "complete" : "failed", process.exitValue()));
        return process.exitValue();
    }

    private static ProcessBuilder getBuilder(String script, Task task) {
        return getBuilder(script, task.getConfiguration());
    }

    private static ProcessBuilder getBuilder(String script, CompileTimeConfiguration configuration) {
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", Joiner.on("&&").join(script.split("\n")));
        builder.directory(configuration.getBasePath().toFile());
        return redirectIo(builder, configuration);
    }

    private static ProcessBuilder redirectIo(ProcessBuilder builder, CompileTimeConfiguration configuration) {
        return redirectIo(builder, configuration.datapipeConfiguration.isDebug());
    }

    private static ProcessBuilder redirectIo(ProcessBuilder builder, boolean isDebug) {
        if(isDebug) {
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        return builder;
    }
}
