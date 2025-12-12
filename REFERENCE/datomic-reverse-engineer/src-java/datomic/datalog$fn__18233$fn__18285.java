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
import datomic.datalog$fn__18233$fn__18285$fn__18286;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18285
extends AFunction {
    Object const_attrid;
    Object whilee;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");

    public datalog$fn__18233$fn__18285(Object object, Object object2, Object object3) {
        this.const_attrid = object;
        this.whilee = object2;
        this.db = object3;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        datalog$fn__18233$fn__18285 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)new datalog$fn__18233$fn__18285$fn__18286(this_.const_attrid, this_.whilee), (Object)((IDb)this_.db).seekAEVT((IDatum)object));
    }
}

