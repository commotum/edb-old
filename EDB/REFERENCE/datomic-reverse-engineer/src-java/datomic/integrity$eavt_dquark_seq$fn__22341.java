/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$eavt_dquark_seq$fn__22341
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"datom");

    public Object invoke(Object p1__22340_SHARP_) {
        Object object = p1__22340_SHARP_;
        p1__22340_SHARP_ = null;
        integrity$eavt_dquark_seq$fn__22341 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1);
    }
}

