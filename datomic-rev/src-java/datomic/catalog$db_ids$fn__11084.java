/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class catalog$db_ids$fn__11084
extends AFunction {
    Object catalog;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Keyword const__1 = RT.keyword(null, (String)"db-id");

    public catalog$db_ids$fn__11084(Object object) {
        this.catalog = object;
    }

    public Object invoke(Object p1__11083_SHARP_) {
        Object object = p1__11083_SHARP_;
        p1__11083_SHARP_ = null;
        catalog$db_ids$fn__11084 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.catalog, (Object)Tuple.create((Object)object, (Object)const__1));
    }
}

