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

public final class garbage$append_leaf$create__19793
extends AFunction {
    Object cluster;
    public static final Var const__0 = RT.var((String)"datomic.garbage", (String)"create-garbage-node");

    public garbage$append_leaf$create__19793(Object object) {
        this.cluster = object;
    }

    public Object invoke(Object uuid, Object val) {
        Object object = uuid;
        uuid = null;
        Object object2 = val;
        val = null;
        garbage$append_leaf$create__19793 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cluster, object, object2);
    }
}

