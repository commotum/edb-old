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
import datomic.db$prefetch_constituents$fn__13936$fn__13937;

public final class db$prefetch_constituents$fn__13936
extends AFunction {
    Object db;
    Object e;
    Object adder;
    Object dispatcher;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"prefetch!");

    public db$prefetch_constituents$fn__13936(Object object, Object object2, Object object3, Object object4) {
        this.db = object;
        this.e = object2;
        this.adder = object3;
        this.dispatcher = object4;
    }

    public Object invoke(Object attr) {
        Object object = attr;
        attr = null;
        db$prefetch_constituents$fn__13936 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.dispatcher, (Object)new db$prefetch_constituents$fn__13936$fn__13937(this_.db, this_.e, this_.adder, object));
    }
}

