/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Util;

public final class config$fn__833
extends AFunction {
    public static Object invokeStatic(Object p1__783_SHARP_) {
        Object object = p1__783_SHARP_;
        p1__783_SHARP_ = null;
        return Util.equiv((Object)object, (Object)"folsom") ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__833.invokeStatic(object2);
    }
}

