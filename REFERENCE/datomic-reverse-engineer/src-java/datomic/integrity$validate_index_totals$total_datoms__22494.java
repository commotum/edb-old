/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class integrity$validate_index_totals$total_datoms__22494
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"index-datoms"), (Object)RT.keyword(null, (String)"mid-index-datoms"), (Object)RT.keyword(null, (String)"history-datoms"));

    public Object invoke(Object p1__22493_SHARP_) {
        Object object = p1__22493_SHARP_;
        p1__22493_SHARP_ = null;
        integrity$validate_index_totals$total_datoms__22494 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object, (Object)const__7)));
    }
}

