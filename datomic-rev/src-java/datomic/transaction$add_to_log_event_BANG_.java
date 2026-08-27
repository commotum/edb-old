/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class transaction$add_to_log_event_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"get-from-cache");
    public static final Var const__1 = RT.var((String)"datomic.transaction", (String)"log-event-map");
    public static final Var const__2 = RT.var((String)"datomic.cache", (String)"put");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"merge");

    public static Object invokeStatic(Object id, Object m) {
        Object tx_info = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), id, (Object)PersistentArrayMap.EMPTY);
        Object object = id;
        id = null;
        Object object2 = tx_info;
        tx_info = null;
        Object object3 = m;
        m = null;
        return ((IFn)const__2.getRawRoot()).invoke(const__1.getRawRoot(), object, ((IFn)const__3.getRawRoot()).invoke(object2, object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return transaction$add_to_log_event_BANG_.invokeStatic(object3, object4);
    }
}

