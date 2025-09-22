/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$resolve_id
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(Object db2, Object x) {
        Object or__5238__auto__18170;
        Object object = x;
        if (object == null) return null;
        if (object == Boolean.FALSE) return null;
        Object object2 = db2;
        db2 = null;
        Object object3 = or__5238__auto__18170 = ((IFn)const__0.getRawRoot()).invoke(object2, x);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = or__5238__auto__18170;
            return object4;
        }
        Object object5 = x;
        x = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__1.getRawRoot()).invoke((Object)"Cannot resolve key: ", object5));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$resolve_id.invokeStatic(object3, object4);
    }
}

