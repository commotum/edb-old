/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.CoerceV;

public final class db$coerce_tuple$fn__13818
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object p1__13817_SHARP_) {
        v0 = p1__13817_SHARP_;
        if (Util.classOf((Object)v0) == db$coerce_tuple$fn__13818.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof CoerceV)) {
            v0 = v0;
            db$coerce_tuple$fn__13818.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = db$coerce_tuple$fn__13818.const__0.getRawRoot().invoke(v0);
        } else {
            v1 = ((CoerceV)v0).coerce_v();
        }
        v2 = or__5238__auto__13820 = v1;
        if (v2 != null && v2 != Boolean.FALSE) {
            v3 = or__5238__auto__13820;
            or__5238__auto__13820 = null;
        } else {
            v3 = p1__13817_SHARP_;
            var1_1 = null;
        }
        return v3;
    }

    static {
        const__0 = RT.var((String)"datomic.db", (String)"coerce-v");
    }
}

