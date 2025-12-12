/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class anomalies$ok__GT_$fn__19452
extends AFunction {
    Object g;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"clojure.core", (String)"or");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.core2.anomalies", (String)"anom");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"clojure.core", (String)"->");

    public anomalies$ok__GT_$fn__19452(Object object) {
        this.g = object;
    }

    public Object invoke(Object step) {
        Object object = step;
        step = null;
        anomalies$ok__GT_$fn__19452 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke(this_.g)))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke(this_.g), ((IFn)const__2.getRawRoot()).invoke(object))))));
    }
}

