/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.valcache$start_server$accept_loop__9804$fn__9805;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$start_server$accept_loop__9804
extends AFunction {
    Object ssc;
    Object internal_shutdown;
    Object ip_validator;
    Object socket_loop;
    Object socket_registry;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"remote-ip");
    public static final Var const__1 = RT.var((String)"datomic.async", (String)"daemon");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__3 = RT.keyword(null, (String)"event");
    public static final Keyword const__4 = RT.keyword((String)"valcache", (String)"reject-ip");
    public static final Keyword const__5 = RT.keyword(null, (String)"ip");

    public valcache$start_server$accept_loop__9804(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.ssc = object;
        this.internal_shutdown = object2;
        this.ip_validator = object3;
        this.socket_loop = object4;
        this.socket_registry = object5;
    }

    public Object invoke() {
        Object var4_5;
        try {
            while (true) {
                SocketChannel sc = ((ServerSocketChannel)this.ssc).accept();
                Object ip = ((IFn)const__0.getRawRoot()).invoke((Object)sc);
                Object object = ((IFn)this.ip_validator).invoke(ip);
                if (object != null && object != Boolean.FALSE) {
                    ((ConcurrentHashMap)this.socket_registry).put(sc, sc);
                    SocketChannel socketChannel = sc;
                    sc = null;
                    ((IFn)const__1.getRawRoot()).invoke((Object)new valcache$start_server$accept_loop__9804$fn__9805(socketChannel, this.socket_loop), (Object)"valcache-socket-loop");
                    continue;
                }
                Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__3;
                    objectArray[1] = const__4;
                    objectArray[2] = const__5;
                    Object object2 = ip;
                    ip = null;
                    objectArray[3] = object2;
                    logger2.info((String)((IFn)const__2.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                }
                SocketChannel socketChannel = sc;
                sc = null;
                ((AbstractInterruptibleChannel)socketChannel).close();
            }
        }
        catch (AsynchronousCloseException _) {
            var4_5 = null;
        }
        finally {
            ((IFn)this.internal_shutdown).invoke();
        }
        return var4_5;
    }
}

