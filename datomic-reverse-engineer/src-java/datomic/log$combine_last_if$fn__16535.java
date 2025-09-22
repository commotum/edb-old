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
import datomic.log$combine_last_if$fn__16535$fn__16536;

public final class log$combine_last_if$fn__16535
extends AFunction {
    Object combine;
    Object pred;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"volatile!");

    public log$combine_last_if$fn__16535(Object object, Object object2) {
        this.combine = object;
        this.pred = object2;
    }

    public Object invoke(Object rf) {
        Object tail_ref = ((IFn)const__0.getRawRoot()).invoke(null);
        Object object = rf;
        rf = null;
        Object object2 = tail_ref;
        tail_ref = null;
        return new log$combine_last_if$fn__16535$fn__16536(object, this.combine, object2, this.pred);
    }
}

