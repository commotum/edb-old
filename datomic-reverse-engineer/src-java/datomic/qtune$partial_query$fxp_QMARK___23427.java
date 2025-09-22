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

public final class qtune$partial_query$fxp_QMARK___23427
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");

    public Object invoke(Object p1__23425_SHARP_) {
        Object object = p1__23425_SHARP_;
        p1__23425_SHARP_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(object) instanceof List ? Boolean.TRUE : Boolean.FALSE;
    }
}

