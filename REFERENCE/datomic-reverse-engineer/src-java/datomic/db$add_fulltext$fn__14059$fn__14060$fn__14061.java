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
import datomic.impl.db.IDatum;

public final class db$add_fulltext$fn__14059$fn__14060$fn__14061
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"fulltext?");

    public db$add_fulltext$fn__14059$fn__14060$fn__14061(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__14055_SHARP_) {
        Object object = p1__14055_SHARP_;
        p1__14055_SHARP_ = null;
        db$add_fulltext$fn__14059$fn__14060$fn__14061 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)((IDatum)object).getA());
    }
}

