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

public final class external_sort_datoms$consume_sorted_datoms$fn__14513
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"reader-iter");
    public static final Var const__1 = RT.var((String)"datomic.external-sort-datoms", (String)"datom-read-handlers");

    public Object invoke(Object p1__14509_SHARP_) {
        Object object = p1__14509_SHARP_;
        p1__14509_SHARP_ = null;
        external_sort_datoms$consume_sorted_datoms$fn__14513 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1.getRawRoot());
    }
}

