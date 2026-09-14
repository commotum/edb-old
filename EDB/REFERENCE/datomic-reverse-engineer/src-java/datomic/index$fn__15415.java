/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;

public final class index$fn__15415
extends AFunction {
    public static Object invokeStatic(Object p1__15414_SHARP_) {
        Object object = p1__15414_SHARP_;
        p1__15414_SHARP_ = null;
        return Numbers.lte((long)1L, (Object)object) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$fn__15415.invokeStatic(object2);
    }
}

