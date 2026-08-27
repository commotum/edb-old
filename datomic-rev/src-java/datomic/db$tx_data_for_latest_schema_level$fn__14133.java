/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$tx_data_for_latest_schema_level$fn__14133
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"db", (String)"id"), "datomic.tx", RT.keyword((String)"db", (String)"system-tx"), RT.keyword(null, (String)"schema")});

    public Object invoke(Object p1__14132_SHARP_) {
        Object object = p1__14132_SHARP_;
        p1__14132_SHARP_ = null;
        db$tx_data_for_latest_schema_level$fn__14133 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__4);
    }
}

