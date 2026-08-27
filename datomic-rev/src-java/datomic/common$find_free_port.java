/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.net.ServerSocket;

public final class common$find_free_port
extends AFunction {
    public static Object invokeStatic() {
        Integer n;
        ServerSocket s = new ServerSocket(RT.uncheckedIntCast((long)0L));
        try {
            n = s.getLocalPort();
        }
        finally {
            ServerSocket serverSocket = s;
            s = null;
            serverSocket.close();
        }
        return n;
    }

    public Object invoke() {
        return common$find_free_port.invokeStatic();
    }
}

