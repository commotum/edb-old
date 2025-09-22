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

public final class db$finish_init
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"add-system-eids");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"add-upgrade-data");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"bootstrap-data-upgrades");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, const__2.getRawRoot()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$finish_init.invokeStatic(object2);
    }
}

