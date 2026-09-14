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
import datomic.datalog$fn__18233$fn__18300$fn__18301;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18300
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");

    public datalog$fn__18233$fn__18300(Object object) {
        this.db = object;
    }

    public Object invoke(Object d) {
        datalog$fn__18233$fn__18300$fn__18301 datalog$fn__18233$fn__18300$fn__18301 = new datalog$fn__18233$fn__18300$fn__18301(d);
        Object object = d;
        d = null;
        datalog$fn__18233$fn__18300 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)datalog$fn__18233$fn__18300$fn__18301, (Object)((IDb)this_.db).seekAEVT((IDatum)object));
    }
}

