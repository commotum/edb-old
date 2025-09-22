/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db$add_constituents$fn__13127
extends AFunction {
    Object comp_id;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-attrid");

    public db$add_constituents$fn__13127(Object object, Object object2) {
        this.comp_id = object;
        this.db = object2;
    }

    public Object invoke(Object constituent) {
        Object object = constituent;
        constituent = null;
        return Tuple.create((Object)((IFn)const__0.getRawRoot()).invoke(this.db, object), (Object)RT.set((Object[])new Object[]{this.comp_id}));
    }
}

