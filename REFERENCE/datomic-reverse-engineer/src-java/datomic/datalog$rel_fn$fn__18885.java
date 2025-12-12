/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datalog$rel_fn$fn__18885
extends RestFn {
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");

    public datalog$rel_fn$fn__18885(Object object) {
        this.f = object;
    }

    public Object doInvoke(Object _, Object args) {
        Object ret = ((IFn)const__0.getRawRoot()).invoke(this.f, args);
        Object object = args;
        args = null;
        Object object2 = ret;
        ret = null;
        return Tuple.create((Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)PersistentVector.EMPTY, object), object2));
    }

    public int getRequiredArity() {
        return 1;
    }
}

