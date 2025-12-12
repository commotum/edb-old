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

public final class peer$resolve_tempid
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-id");

    public static Object invokeStatic(Object db2, Object tempids, Object tempid2) {
        Object object;
        Object object2 = tempids;
        tempids = null;
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(tempid2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = tempid2;
            tempid2 = null;
        } else {
            Object object4 = db2;
            db2 = null;
            Object object5 = tempid2;
            tempid2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object4, object5);
        }
        return RT.get((Object)object2, (Object)object);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return peer$resolve_tempid.invokeStatic(object4, object5, object6);
    }
}

