/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class support$disallow_find_variants_BANG_$fn__19075
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vector?");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)".");

    public Object invoke(Object p1__19074_SHARP_) {
        Object object;
        Object or__5238__auto__19077;
        Object object2 = or__5238__auto__19077 = ((IFn)const__0.getRawRoot()).invoke(p1__19074_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__19077;
            or__5238__auto__19077 = null;
        } else {
            Object object3 = p1__19074_SHARP_;
            p1__19074_SHARP_ = null;
            support$disallow_find_variants_BANG_$fn__19075 this_ = null;
            object = Util.equiv((Object)const__2, (Object)object3) ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }
}

