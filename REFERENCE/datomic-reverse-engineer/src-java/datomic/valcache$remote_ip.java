/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public final class valcache$remote_ip
extends AFunction {
    public static Object invokeStatic(Object sc) {
        SocketAddress inet;
        Object object = sc;
        sc = null;
        SocketAddress socketAddress = inet = ((SocketChannel)object).getRemoteAddress();
        inet = null;
        return ((InetSocketAddress)socketAddress).getHostString();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$remote_ip.invokeStatic(object2);
    }
}

