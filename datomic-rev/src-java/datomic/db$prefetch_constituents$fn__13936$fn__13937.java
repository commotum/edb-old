/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$prefetch_constituents$fn__13936$fn__13937
extends AFunction {
    Object db;
    Object e;
    Object adder;
    Object attr;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"ea->v");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"long-add!");

    public db$prefetch_constituents$fn__13936$fn__13937(Object object, Object object2, Object object3, Object object4) {
        this.db = object;
        this.e = object2;
        this.adder = object3;
        this.attr = object4;
    }

    public Object invoke() {
        long start__13414__auto__13939 = System.nanoTime();
        Object ret__13415__auto__13940 = ((IFn)const__0.getRawRoot()).invoke(this.db, this.e, this.attr);
        ((IFn.OLO)const__1.getRawRoot()).invokePrim(this.adder, System.nanoTime() - start__13414__auto__13939);
        Object var3_2 = null;
        return ret__13415__auto__13940;
    }
}

