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
import datomic.datalog$fn__18233$fn__18312$fn__18313;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18312
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");

    public datalog$fn__18233$fn__18312(Object object) {
        this.db = object;
    }

    public Object invoke(Object d) {
        datalog$fn__18233$fn__18312$fn__18313 datalog$fn__18233$fn__18312$fn__18313 = new datalog$fn__18233$fn__18312$fn__18313(d);
        Object object = d;
        d = null;
        datalog$fn__18233$fn__18312 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)datalog$fn__18233$fn__18312$fn__18313, (Object)((IDb)this_.db).seekRAET((IDatum)object));
    }
}

