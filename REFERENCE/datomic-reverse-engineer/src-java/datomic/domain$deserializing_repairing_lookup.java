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

public final class domain$deserializing_repairing_lookup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"->DeserializingRepairingLookup");

    public static Object invokeStatic(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$deserializing_repairing_lookup.invokeStatic(object2);
    }
}

