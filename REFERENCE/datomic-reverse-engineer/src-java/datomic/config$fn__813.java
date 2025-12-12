/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.Util;

public final class config$fn__813
extends AFunction {
    public static Object invokeStatic(Object p1__780_SHARP_) {
        Boolean bl;
        boolean or__5238__auto__815 = Util.identical((Object)p1__780_SHARP_, null);
        if (or__5238__auto__815) {
            bl = or__5238__auto__815 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object = p1__780_SHARP_;
            p1__780_SHARP_ = null;
            bl = Numbers.isPos((Object)object) ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__813.invokeStatic(object2);
    }
}

