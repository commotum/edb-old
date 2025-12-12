/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ExceptionInfo
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ExceptionInfo;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class peer$peer_queue_exceeded
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"peer-queue-exceeded");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"anomalize");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword((String)"db", (String)"error");
    public static final Object const__5 = RT.classForName((String)"clojure.lang.ExceptionInfo");

    public static Object invokeStatic() {
        String msg__662__auto__21488 = "Peer work queue limit exceeded, transactor may be unavailable.";
        String string = (String)((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)" ", (Object)msg__662__auto__21488);
        String string2 = msg__662__auto__21488;
        msg__662__auto__21488 = null;
        return new ExceptionInfo(string, (IPersistentMap)((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(null, (Object)const__4, (Object)const__1), const__5, (Object)string2));
    }

    public Object invoke() {
        return peer$peer_queue_exceeded.invokeStatic();
    }
}

