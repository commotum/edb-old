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

public final class garbage$gc_deleted_db$status__19883
extends RestFn {
    Object status_callback;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    public garbage$gc_deleted_db$status__19883(Object object) {
        this.status_callback = object;
    }

    public Object doInvoke(Object args) {
        Object object = args;
        args = null;
        garbage$gc_deleted_db$status__19883 this_ = null;
        return ((IFn)this_.status_callback).invoke(((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object));
    }

    public int getRequiredArity() {
        return 0;
    }
}

