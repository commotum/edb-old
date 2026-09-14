/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.monitor.Statistics;

public final class monitor$add_stat
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"k");
    public static final Var const__3 = RT.var((String)"datomic.monitor", (String)"metric-event-callback");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"datomic.monitor", (String)"statistics");

    public static Object invokeStatic(Object k, Object val) {
        Object temp__5457__auto__570;
        Object object = k;
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", val, (Object)"\n", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object2 = temp__5457__auto__570 = const__3.getRawRoot();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object cb;
            Object object3 = temp__5457__auto__570;
            temp__5457__auto__570 = null;
            Object object4 = cb = object3;
            cb = null;
            ((IFn)object4).invoke(k, val);
        }
        Object object5 = k;
        k = null;
        Object object6 = val;
        val = null;
        return ((Statistics)((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot())).addObservation(object5, object6);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return monitor$add_stat.invokeStatic(object3, object4);
    }
}

