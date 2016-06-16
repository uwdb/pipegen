package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Lists;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;

import java.util.Map;

public class Debugger {
    public static VirtualMachine attachDebugger(String hostname) {
        return attachDebugger(hostname, 5005);
    }

    public static VirtualMachine attachDebugger(String hostname, int port) {
        VirtualMachineManager vmm = com.sun.jdi.Bootstrap.virtualMachineManager();
        AttachingConnector atconn = null;
        for(AttachingConnector c: vmm.attachingConnectors())
            if("dt_socket".equalsIgnoreCase(c.transport().name())) {
                atconn = c;
            }
        Map<String, Connector.Argument> prm = atconn.defaultArguments();
        prm.get("hostname").setValue(hostname);
        prm.get("port").setValue(Integer.toString(port));
        VirtualMachine vm2 = null;
        try {
            vm2 = atconn.attach(prm);

            ///for(ThreadReference t: vm2.allThreads()) {
            //    log.info(t.name());
            //    if (t.name().equals("main"))
            //        t.suspend();
            //}
            vm2.resume();
        } catch(Exception e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        }

        return vm2;
    }

    public void sleepThread(VirtualMachine vm, long delay) {
        vm.suspend();
        ReferenceType r = vm.classesByName("java.lang.Thread").get(0);
        ClassType cr = (ClassType)r;
        ///ClassObjectReference cr = r.classObject(); //.invokeMethod(t, )
        Method m = r.methodsByName("sleep").get(0);
        for(ThreadReference t: vm.allThreads()) {
            if (t.name().equals("main")) {
                try {
                    cr.invokeMethod(t, m, Lists.newArrayList(vm.mirrorOf(delay)), 0);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                t.suspend();
            }
        }
        //vm2.resume();
        try {
            Thread.sleep(5000);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
