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
import java.lang.reflect.Method;

public final class datafy$setters$fn__17159
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__2 = 0L;
    public static final Object const__3 = 3L;

    public Object invoke(Object p1__17158_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__17161 = Util.equiv((Object)"set", (Object)((IFn)const__1.getRawRoot()).invoke((Object)((Method)p1__17158_SHARP_).getName(), const__2, const__3));
        if (and__5236__auto__17161) {
            Object object = p1__17158_SHARP_;
            p1__17158_SHARP_ = null;
            datafy$setters$fn__17159 this_ = null;
            bl = Util.equiv((long)1L, (long)RT.count(((Method)object).getParameterTypes())) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__17161 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

