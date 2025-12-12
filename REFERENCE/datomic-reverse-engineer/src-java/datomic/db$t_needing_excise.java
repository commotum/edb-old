/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;
import java.io.Serializable;

public final class db$t_needing_excise
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"eid->eidx");

    public static Object invokeStatic(Object db2, Object d) {
        Serializable serializable;
        boolean and__5236__auto__13174 = Util.equiv((Object)((Datom)d).a(), (long)15L);
        if (and__5236__auto__13174) {
            Object object = d;
            d = null;
            serializable = Numbers.num((long)((IFn.LL)const__2.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)((Datom)object).tx()))));
        } else {
            serializable = and__5236__auto__13174 ? Boolean.TRUE : Boolean.FALSE;
        }
        return serializable;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$t_needing_excise.invokeStatic(object3, object4);
    }
}

