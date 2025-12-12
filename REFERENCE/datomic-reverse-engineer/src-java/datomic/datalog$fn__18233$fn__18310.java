/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18310
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"asserting-datum");

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        return ((IFn.LLOLO)const__0.getRawRoot()).invokePrim(Long.MIN_VALUE, -1L, ((IDatum)object).getV(), 0x1FFFFFFFFFFFFFFFL);
    }
}

