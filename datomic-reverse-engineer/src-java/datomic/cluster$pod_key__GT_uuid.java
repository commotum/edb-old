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
import java.util.UUID;

public final class cluster$pod_key__GT_uuid
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__1 = 4L;

    public static Object invokeStatic(Object pod_key) {
        Object object = pod_key;
        pod_key = null;
        return UUID.fromString((String)((IFn)const__0.getRawRoot()).invoke(object, const__1));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$pod_key__GT_uuid.invokeStatic(object2);
    }
}

