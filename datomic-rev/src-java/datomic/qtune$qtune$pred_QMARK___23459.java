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
import java.util.List;

public final class qtune$qtune$pred_QMARK___23459
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"=");
    public static final Object const__1 = 1L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");

    public Object invoke(Object p1__23452_SHARP_) {
        Integer n = RT.count((Object)p1__23452_SHARP_);
        Object object = p1__23452_SHARP_;
        p1__23452_SHARP_ = null;
        qtune$qtune$pred_QMARK___23459 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1, (Object)n, (Object)(((IFn)const__5.getRawRoot()).invoke(object) instanceof List ? Boolean.TRUE : Boolean.FALSE));
    }
}

