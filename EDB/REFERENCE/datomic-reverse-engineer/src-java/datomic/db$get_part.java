/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$OL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$get_part
extends AFunction
implements IFn.OL {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"eid->part");

    public static long invokeStatic(Object datum2) {
        Object object = datum2;
        datum2 = null;
        return ((IFn.LL)const__0.getRawRoot()).invokePrim(((IDatum)object).getE());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return new Long(db$get_part.invokeStatic(object2));
    }

    public final long invokePrim(Object object) {
        Object object2 = object;
        object = null;
        return db$get_part.invokeStatic(object2);
    }
}

