/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.valcache$start_server$socket_loop__9798$fn__9800;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class valcache$start_server$socket_loop__9798
extends AFunction {
    Object sasl;
    Object handled;
    Object path;
    Object sem;
    Object socket_registry;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"sasl-loop");
    public static final Object const__2 = 24L;
    public static final Var const__3 = RT.var((String)"datomic.valcache", (String)"read-header");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__7 = RT.keyword(null, (String)"opcode");
    public static final Var const__10 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__11 = RT.keyword(null, (String)"event");
    public static final Keyword const__12 = RT.keyword((String)"valcache", (String)"io-exception");
    public static final Keyword const__13 = RT.keyword(null, (String)"msg");
    public static final AFn const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"valcache", (String)"socket-exception")});
    public static final Var const__16 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public valcache$start_server$socket_loop__9798(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.sasl = object;
        this.handled = object2;
        this.path = object3;
        this.sem = object4;
        this.socket_registry = object5;
    }

    public Object invoke(Object sc) {
        Object var7_14;
        try {
            Object v8;
            block14: {
                ((SocketChannel)sc).socket().setTcpNoDelay(Boolean.TRUE);
                Object object = this.sasl;
                Object object2 = object != null && object != Boolean.FALSE ? ((IFn)const__0.getRawRoot()).invoke(this.sasl, sc, this.sem) : Boolean.TRUE;
                if (object2 != null && object2 != Boolean.FALSE) {
                    ByteBuffer hb = ByteBuffer.wrap(Numbers.byte_array((Object)const__2));
                    while (true) {
                        Object map__9799;
                        Object object3;
                        Object map__97992 = ((IFn)const__3.getRawRoot()).invoke(sc, (Object)hb);
                        Object object4 = ((IFn)const__4.getRawRoot()).invoke(map__97992);
                        if (object4 != null && object4 != Boolean.FALSE) {
                            Object object5 = map__97992;
                            map__97992 = null;
                            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__5.getRawRoot()).invoke(object5)));
                        } else {
                            object3 = map__97992;
                            map__97992 = null;
                        }
                        Object header = map__9799 = object3;
                        Object object6 = map__9799;
                        map__9799 = null;
                        Object opcode = RT.get((Object)object6, (Object)const__7);
                        ((Semaphore)this.sem).acquire();
                        Object object7 = header;
                        header = null;
                        ((IFn)new valcache$start_server$socket_loop__9798$fn__9800(sc, object7, this.handled, this.path, this.sem)).invoke();
                        Object object8 = opcode;
                        opcode = null;
                        if (Util.equiv((Object)object8, (long)7L)) {
                            v8 = null;
                            break block14;
                        }
                        ByteBuffer byteBuffer = hb;
                        hb = null;
                        hb = byteBuffer;
                    }
                }
                v8 = null;
            }
            var7_14 = v8;
        }
        catch (IOException ex2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__11;
                objectArray[1] = const__12;
                objectArray[2] = const__13;
                Object ex2 = null;
                objectArray[3] = ((Throwable)ex2).getMessage();
                logger2.info((String)((IFn)const__10.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            var7_14 = null;
        }
        catch (Throwable ex3) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.valcache");
            Object ex3 = null;
            Throwable ex4 = ex3;
            if (logger.isInfoEnabled()) {
                logger.info((String)((IFn)const__10.getRawRoot()).invoke((Object)const__15), ex4);
                Logger logger3 = logger;
                logger = null;
                Throwable throwable = ex4;
                ex4 = null;
                ((IFn)const__16.getRawRoot()).invoke((Object)logger3, (Object)throwable);
            }
            var7_14 = null;
        }
        finally {
            ((AbstractInterruptibleChannel)sc).close();
            Object object = sc;
            sc = null;
            ((ConcurrentHashMap)this.socket_registry).remove(object);
        }
        return var7_14;
    }
}

