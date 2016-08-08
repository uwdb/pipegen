package org.brandonhaynes.pipegen.utilities;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sun.jvmstat.monitor.*;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class HostListener implements sun.jvmstat.monitor.event.HostListener {
    private static final Logger log = Logger.getLogger(HostListener.class.getName());
    private final MonitoredHost host;
    private final Predicate<Pair<String, String>> predicate;
    private final Function<Integer, Boolean> action;
    private final int timeout;
    private AtomicInteger actionsSucceeding = new AtomicInteger(0);
    private AtomicInteger actionsInProgress = new AtomicInteger(0);

    public HostListener(Predicate<Pair<String, String>> predicate, Function<Integer, Boolean> action, int timeout)
            throws MonitorException {
        this(MonitoredHost.getMonitoredHost(getDefaultHostIdentifier()), predicate, action, timeout);
    }

    private HostListener(MonitoredHost host, Predicate<Pair<String, String>> predicate,
                        Function<Integer, Boolean> action,
                        int timeout) throws MonitorException {
        this.host = host;
        this.timeout = timeout;
        this.predicate = predicate;
        this.action = action;
        host.addHostListener(this);
    }

    public void vmStatusChanged(VmStatusChangeEvent event) {
        try {
            for (Object pid : event.getStarted()) {
                String vmId = "//" + pid.toString() + "?mode=r";
                VmIdentifier id = new VmIdentifier(vmId);
                MonitoredVm vm = host.getMonitoredVm(id, 0);

                if (predicate.test(new ImmutablePair<>(MonitoredVmUtil.mainClass(vm, true),
                                                       MonitoredVmUtil.commandLine(vm))))
                    try {
                        log.info(String.format("Attaching to %s %s", MonitoredVmUtil.mainClass(vm, true),
                                                                     MonitoredVmUtil.commandLine(vm)));
                        actionsInProgress.incrementAndGet();
                        actionsSucceeding.addAndGet(action.apply((Integer) pid) ? 1 : 0);
                    } finally {
                        actionsInProgress.decrementAndGet();
                    }
            }
        } catch (MonitorException | URISyntaxException e) {
            actionsInProgress.set(0);
            throw new RuntimeException(e);
        }
    }

    public void disconnected(HostEvent event) {
    }

    public int join() throws InterruptedException, MonitorException {
        int wait = 0;
        while (wait++ < timeout && actionsInProgress.get() > 0) {
            log.info(String.format("%d actions in progress (%d)", actionsInProgress.get(), wait));
            Thread.sleep(1000);
        }
        host.removeHostListener(this);
        return actionsSucceeding.get();
    }

    private static HostIdentifier getDefaultHostIdentifier() {
        try {
            return new HostIdentifier("//localhost");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
