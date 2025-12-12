/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class datalog$rel_pred$fn__18888
extends RestFn {
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");

    public datalog$rel_pred$fn__18888(Object object) {
        this.f = object;
    }

    public Object doInvoke(Object _, Object args) {
        Object object = args;
        args = null;
        datalog$rel_pred$fn__18888 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.f, object);
    }

    public int getRequiredArity() {
        return 1;
    }
}

