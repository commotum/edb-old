/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class db$bootstrap_db_STAR_$fn__14158
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj");

    public db$bootstrap_db_STAR_$fn__14158(Object object) {
        this.db = object;
    }

    public Object invoke(Object ret, Object d) {
        Object object;
        Object attr;
        int a = ((IDatum)d).getA();
        Object object2 = attr = ((IDbImpl)this_.db).elementAt(a);
        attr = null;
        if (Util.equiv((Object)((Attribute)object2).vtypeid, (long)20L)) {
            Object object3 = ret;
            ret = null;
            Object object4 = d;
            d = null;
            db$bootstrap_db_STAR_$fn__14158 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3, object4);
        } else {
            object = ret;
            Object var1_1 = null;
        }
        return object;
    }
}

