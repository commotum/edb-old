/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$retry$fn__20043
extends AFunction {
    public static final Object const__2 = 50L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"rand-int");
    public static final Object const__4 = 2L;

    public Object invoke(Object p1__20040_SHARP_) {
        Object object = p1__20040_SHARP_;
        p1__20040_SHARP_ = null;
        backup$retry$fn__20043 this_ = null;
        return Numbers.multiply((Object)Numbers.add((long)50L, (Object)((IFn)const__3.getRawRoot()).invoke(const__2)), (double)Math.pow(RT.doubleCast((Object)((Number)const__4)), RT.doubleCast((Object)((Number)object))));
    }
}

