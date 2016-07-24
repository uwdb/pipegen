package org.brandonhaynes.pipegen.instrumentation;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.sun.btrace.CommandListener;
import com.sun.btrace.client.Client;
import com.sun.btrace.comm.Command;
import com.sun.btrace.comm.DataCommand;
import com.sun.btrace.comm.ErrorCommand;

import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class OperationTracer {
	private static final int DEFAULT_PORT = 7777;
	private static final Logger log = Logger.getLogger(OperationTracer.class.getName());

	public static TraceResult traceOperation(int processId, Path traceFile, Collection<Path> classPaths,
											 Path agentFile, int timeout)
			throws IOException {
		return traceOperation(processId, traceFile, classPaths, agentFile, timeout, false);
	}

	public static TraceResult traceOperation(int processId, Path traceFile, Collection<Path> classPaths, Path agentFile,
											 int timeout, boolean debug)
			throws IOException {
		return traceOperation(processId, DEFAULT_PORT, traceFile, classPaths, agentFile, timeout, debug);
	}

	public static TraceResult traceOperation(int processId, int clientPort, Path traceFile,
											 Collection<Path> classPaths, Path agentFile,
											 int timeout, boolean debug)
			throws IOException {
		log.info("Attaching to process " + Integer.toString(processId));

		StringWriter output = new StringWriter();
		String classPath = Joiner.on(':').join(classPaths);
        Client client = new Client(clientPort, ".", debug, false, true, false, null, null);
		byte[] bytecode = client.compile(traceFile.toString(), classPath, new PrintWriter(output));

		if(bytecode == null)
			throw new IOException(String.format("No bytecode produced during compilation for %s: %s",
					traceFile, output));
		else {
			client.attach(Integer.toString(processId), agentFile.toString(), classPath, null);
			return executeClient(client, bytecode, timeout);
		}
	}

	private static TraceResult executeClient(final Client client, final byte[] bytecode, int timeout)
			throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		final File instrumentationFile = File.createTempFile("instrumentation", null);

		try {
			try(FileWriter writer = new FileWriter(instrumentationFile)) {
				new SimpleTimeLimiter(executor).callWithTimeout((Callable<Void>) () -> {
					try {
						client.submit(null, bytecode, new String[0], new OperationCommandListener(client, writer));
					} catch (EOFException e) {
						log.warning(String.format("Swallowed exception '%s' during ungraceful client exit",
								e.getMessage()));
					}
					return null;
				}, timeout, TimeUnit.SECONDS, false);
			}
		} catch(IOException e) {
			throw e;
		} catch(UncheckedTimeoutException e) {
			System.out.println("DEBUG: client monitor timeout");
		} catch(Exception e) {
			throw new IOException(e);
		} finally {
			try {
    	        client.sendExit(0);
	        } catch(IOException e) {
				log.warning(String.format("Swallowed exception '%s' during ungraceful client exit", e.getMessage()));
	        }
            executor.shutdownNow();
            //process.destroy();
		}

		return new TraceResult(instrumentationFile.toPath());
	}

	private static class OperationCommandListener implements CommandListener {
		final Client client;
		final PrintWriter writer;

		public OperationCommandListener(final Client client, Writer writer) {
			this.client = client;
			this.writer = new PrintWriter(writer);
		}

        @Override
        public void onCommand(Command command) throws IOException {
            if (command instanceof DataCommand)
            	onDataCommand((DataCommand)command);
            else if (command.getType() == Command.EXIT)
            	onExitCommand();
            else if (command.getType() == Command.ERROR)
            	onErrorCommand((ErrorCommand)command);
        }

        private void onDataCommand(DataCommand command) throws IOException {
            command.print(writer);
        }

        private void onExitCommand() {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
		private void onErrorCommand(ErrorCommand command) {
            Throwable cause = command.getCause();
            if (cause != null)
                cause.printStackTrace();
        }
	}
}
