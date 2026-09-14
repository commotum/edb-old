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
import datomic.datalog$fn__18233$fn__18293$fn__18294;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18293
extends AFunction {
    Object const_attrid;
    Object whilev;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");

    public datalog$fn__18233$fn__18293(Object object, Object object2, Object object3) {
        this.const_attrid = object;
        this.whilev = object2;
        this.db = object3;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        datalog$fn__18233$fn__18293 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)new datalog$fn__18233$fn__18293$fn__18294(this_.const_attrid, this_.whilev), (Object)((IDb)this_.db).seekAVET((IDatum)object));
    }
}

