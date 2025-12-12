/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Db;
import datomic.impl.db.IDatum;

public final class db$next_valid_inst
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"find-eavt");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Object const__4 = 50L;
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object db2, Object now) {
        Object object;
        Object object2 = db2;
        Object object3 = db2;
        db2 = null;
        Object basis_inst = ((IDatum)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object2, (Object)Numbers.num((long)((IFn.LLL)const__2.getRawRoot()).invokePrim(3L, ((Db)object3).basisT())), const__4))).getV();
        if (((IFn.OOL)const__6.getRawRoot()).invokePrim(now, basis_inst) > 0L) {
            object = now;
            now = null;
        } else {
            object = basis_inst;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$next_valid_inst.invokeStatic(object3, object4);
    }
}

