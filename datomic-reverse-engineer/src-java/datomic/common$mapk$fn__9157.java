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

public final class common$mapk$fn__9157
extends AFunction {
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");

    public common$mapk$fn__9157(Object object) {
        this.f = object;
    }

    public Object invoke(Object m, Object k) {
        Object object = m;
        m = null;
        Object object2 = k;
        Object object3 = k;
        k = null;
        common$mapk$fn__9157 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)this_.f).invoke(object3));
    }
}

