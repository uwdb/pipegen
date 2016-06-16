package org.brandonhaynes.pipegen.utilities;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sun.jvmstat.monitor.*;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

import java.net.URISyntaxException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class HostListener implements sun.jvmstat.monitor.event.HostListener {
    private static final Logger log = Logger.getLogger(HostListener.class.getName());
    private final MonitoredHost host;
    private final Predicate<Pair<String, String>> predicate;
    private final Function<Integer, Boolean> action;
    private final int timeout;
    private boolean success = false;

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
;
                if (predicate.test(new ImmutablePair<>(MonitoredVmUtil.mainClass(vm, true),
                                                       MonitoredVmUtil.commandLine(vm)))) {
                    log.info("Attaching to " + MonitoredVmUtil.mainClass(vm, true));
                    success = success | action.apply((Integer) pid);
                }
            }
        } catch (MonitorException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnected(HostEvent event) {
    }

    public boolean join() throws InterruptedException, MonitorException {
        int wait = 0;
        while (wait++ < timeout && !success)
            Thread.sleep(1000);
        host.removeHostListener(this);
        return this.success;
    }

    private static HostIdentifier getDefaultHostIdentifier() {
        try {
            return new HostIdentifier("//localhost");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
