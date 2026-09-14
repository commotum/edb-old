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
import datomic.Datom;
import datomic.db.AVof;
import datomic.db.Attribute;
import java.util.HashMap;

public final class db$create_unique_value_validator$fn__13309
extends AFunction {
    Object db;
    Object avmap;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datoms-conflict");

    public db$create_unique_value_validator$fn__13309(Object object, Object object2) {
        this.db = object;
        this.avmap = object2;
    }

    public Object invoke(Object d) {
        Object object;
        boolean and__5236__auto__13311 = ((Datom)d).added();
        Object object2 = and__5236__auto__13311 ? ((Attribute)((IFn)db$create_unique_value_validator$fn__13309.const__0.getRawRoot()).invoke((Object)this_.db, (Object)((Datom)d).a())).unique : (and__5236__auto__13311 ? Boolean.TRUE : Boolean.FALSE);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object v = ((HashMap)this_.avmap).put(new AVof(d), d);
            boolean or__5238__auto__13312 = Util.identical((Object)v, null);
            if (or__5238__auto__13312) {
                object = or__5238__auto__13312 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object3 = v;
                v = null;
                Object object4 = d;
                d = null;
                db$create_unique_value_validator$fn__13309 this_ = null;
                object = ((IFn)const__2.getRawRoot()).invoke(this_.db, object3, object4);
            }
        } else {
            object = Boolean.TRUE;
        }
        return object;
    }
}

