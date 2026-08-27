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
import datomic.datalog$fn__18233$fn__18306$fn__18307;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18306
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");

    public datalog$fn__18233$fn__18306(Object object) {
        this.db = object;
    }

    public Object invoke(Object d) {
        datalog$fn__18233$fn__18306$fn__18307 datalog$fn__18233$fn__18306$fn__18307 = new datalog$fn__18233$fn__18306$fn__18307(d);
        Object object = d;
        d = null;
        Object ret = ((IFn)const__0.getRawRoot()).invoke(this.db, (Object)datalog$fn__18233$fn__18306$fn__18307, (Object)((IDb)this.db).seekEAVT((IDatum)object));
        Object var2_2 = null;
        return ret;
    }
}

