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
import datomic.db.Attribute;
import datomic.db.EAOpof;
import java.util.HashMap;

public final class db$create_card_one_validator$fn__13305
extends AFunction {
    Object db;
    Object eaomap;
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"datoms-conflict");

    public db$create_card_one_validator$fn__13305(Object object, Object object2) {
        this.db = object;
        this.eaomap = object2;
    }

    public Object invoke(Object d) {
        Object object;
        if (Util.equiv((long)35L, (Object)((Attribute)((IFn)db$create_card_one_validator$fn__13305.const__2.getRawRoot()).invoke((Object)this_.db, (Object)((Datom)d).a())).cardinality)) {
            Object v = ((HashMap)this_.eaomap).put(new EAOpof(d), d);
            boolean or__5238__auto__13307 = Util.identical((Object)v, null);
            if (or__5238__auto__13307) {
                object = or__5238__auto__13307 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object2 = v;
                v = null;
                Object object3 = d;
                d = null;
                db$create_card_one_validator$fn__13305 this_ = null;
                object = ((IFn)const__4.getRawRoot()).invoke(this_.db, object2, object3);
            }
        } else {
            object = Boolean.TRUE;
        }
        return object;
    }
}

