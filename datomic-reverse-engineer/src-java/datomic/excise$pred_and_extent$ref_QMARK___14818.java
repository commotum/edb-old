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

public final class excise$pred_and_extent$ref_QMARK___14818
extends AFunction {
    Object db;
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"attribute");

    public excise$pred_and_extent$ref_QMARK___14818(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__14812_SHARP_) {
        Object object = p1__14812_SHARP_;
        p1__14812_SHARP_ = null;
        excise$pred_and_extent$ref_QMARK___14818 this_ = null;
        return Util.equiv((Object)((Attribute)((IFn)excise$pred_and_extent$ref_QMARK___14818.const__1.getRawRoot()).invoke((Object)this_.db, (Object)object)).vtypeid, (long)20L) ? Boolean.TRUE : Boolean.FALSE;
    }
}

