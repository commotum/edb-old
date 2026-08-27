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
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$normalize_or_join$fn__18711
extends AFunction {
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"and");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");

    public Object invoke(Object p1__18706_SHARP_) {
        Object object;
        datalog$normalize_or_join$fn__18711 this_;
        if (Util.equiv((Object)const__1, (Object)((IFn)const__2.getRawRoot()).invoke(p1__18706_SHARP_))) {
            Object object2 = p1__18706_SHARP_;
            p1__18706_SHARP_ = null;
            this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object2);
        } else {
            Object object3 = p1__18706_SHARP_;
            p1__18706_SHARP_ = null;
            this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object3);
        }
        return object;
    }
}

