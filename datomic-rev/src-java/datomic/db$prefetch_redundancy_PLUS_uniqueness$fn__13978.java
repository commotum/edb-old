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
import datomic.db$prefetch_redundancy_PLUS_uniqueness$fn__13978$fn__13979;

public final class db$prefetch_redundancy_PLUS_uniqueness$fn__13978
extends AFunction {
    Object redundancy;
    Object db;
    Object uniqueness;
    Object basis;
    Object dispatcher;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"prefetch!");

    public db$prefetch_redundancy_PLUS_uniqueness$fn__13978(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.redundancy = object;
        this.db = object2;
        this.uniqueness = object3;
        this.basis = object4;
        this.dispatcher = object5;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        db$prefetch_redundancy_PLUS_uniqueness$fn__13978 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.dispatcher, (Object)new db$prefetch_redundancy_PLUS_uniqueness$fn__13978$fn__13979(this_.redundancy, this_.db, this_.uniqueness, this_.basis, object));
    }
}

