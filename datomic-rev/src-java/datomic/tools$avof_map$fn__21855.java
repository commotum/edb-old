/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class tools$avof_map$fn__21855
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"->AVof");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"fnil");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");

    public Object invoke(Object m, Object d) {
        Object object = m;
        m = null;
        IPersistentVector iPersistentVector = Tuple.create((Object)((IFn)const__1.getRawRoot()).invoke(d));
        Object object2 = d;
        d = null;
        tools$avof_map$fn__21855 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)iPersistentVector, ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), (Object)PersistentVector.EMPTY), object2);
    }
}

