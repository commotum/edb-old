/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$fn__20316
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"format");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"upper-case");

    public static Object invokeStatic(Object p1__20315_SHARP_) {
        Object object = ((IFn)const__1.getRawRoot()).invoke((Object)"%02x", p1__20315_SHARP_);
        Object object2 = p1__20315_SHARP_;
        p1__20315_SHARP_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"%02x", object2)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$fn__20316.invokeStatic(object2);
    }
}

