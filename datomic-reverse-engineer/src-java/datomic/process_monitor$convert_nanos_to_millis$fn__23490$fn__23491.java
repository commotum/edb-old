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

public final class process_monitor$convert_nanos_to_millis$fn__23490$fn__23491
extends AFunction {
    Object round;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update");

    public process_monitor$convert_nanos_to_millis$fn__23490$fn__23491(Object object) {
        this.round = object;
    }

    public Object invoke(Object v, Object k) {
        Object object = v;
        v = null;
        Object object2 = k;
        k = null;
        process_monitor$convert_nanos_to_millis$fn__23490$fn__23491 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, this_.round);
    }
}

