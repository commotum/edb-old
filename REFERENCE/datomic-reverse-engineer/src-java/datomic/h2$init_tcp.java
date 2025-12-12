/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.h2.tools.Server
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.h2.tools.Server;

public final class h2$init_tcp
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"h2-port");
    public static final Keyword const__5 = RT.keyword(null, (String)"data-dir");
    public static final Var const__6 = RT.var((String)"datomic.h2", (String)"init-embedded");
    public static final Var const__7 = RT.var((String)"datomic.h2", (String)"ensure-datomic-password");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__10 = RT.var((String)"datomic.h2", (String)"can-remote?");
    public static final AFn const__11 = (AFn)Tuple.create((Object)"-tcpAllowOthers");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__14 = RT.keyword(null, (String)"server");

    public static Object invokeStatic(Object p__11641) {
        Object conn;
        Object map__11642;
        Object object;
        Object object2 = p__11641;
        p__11641 = null;
        Object map__116422 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__116422);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__116422;
            map__116422 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__116422;
            map__116422 = null;
        }
        Object spec = map__11642 = object;
        RT.get((Object)map__11642, (Object)const__3);
        Object h2_port = RT.get((Object)map__11642, (Object)const__4);
        Object object5 = map__11642;
        map__11642 = null;
        Object data_dir = RT.get((Object)object5, (Object)const__5);
        ((IFn)const__6.getRawRoot()).invoke(spec);
        Object object6 = conn = ((IFn)const__7.getRawRoot()).invoke(spec);
        if (object6 == null || object6 == Boolean.FALSE) {
            throw (Throwable)new RuntimeException("Incorrect storage-datomic-password");
        }
        Object object7 = conn;
        conn = null;
        ((AutoCloseable)object7).close();
        Object object8 = ((IFn)const__10.getRawRoot()).invoke(spec);
        Object[] objectArray = new Object[7];
        objectArray[0] = "-ifExists";
        objectArray[1] = "-properties";
        objectArray[2] = "";
        objectArray[3] = "-tcpPort";
        Object object9 = h2_port;
        h2_port = null;
        objectArray[4] = ((IFn)const__12.getRawRoot()).invoke(object9);
        objectArray[5] = "-baseDir";
        Object object10 = data_dir;
        data_dir = null;
        objectArray[6] = object10;
        Server server = Server.createTcpServer((String[])((String[])((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)(object8 != null && object8 != Boolean.FALSE ? const__11 : null), (Object)RT.vector((Object[])objectArray)))));
        server.start();
        Object object11 = spec;
        spec = null;
        Server server2 = server;
        server = null;
        return ((IFn)const__13.getRawRoot()).invoke(object11, (Object)const__14, (Object)server2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$init_tcp.invokeStatic(object2);
    }
}

