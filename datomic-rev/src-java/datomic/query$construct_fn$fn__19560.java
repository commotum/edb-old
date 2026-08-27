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

public final class query$construct_fn$fn__19560
extends RestFn {
    Object qform;
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"q");

    public query$construct_fn$fn__19560(Object object) {
        this.qform = object;
    }

    public Object doInvoke(Object args) {
        Object object = args;
        args = null;
        query$construct_fn$fn__19560 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.qform, object);
    }

    public int getRequiredArity() {
        return 0;
    }
}

