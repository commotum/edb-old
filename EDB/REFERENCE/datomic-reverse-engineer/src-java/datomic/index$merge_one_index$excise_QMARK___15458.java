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
import datomic.index$merge_one_index$excise_QMARK___15458$fn__15459;

public final class index$merge_one_index$excise_QMARK___15458
extends AFunction {
    Object xpreds;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");

    public index$merge_one_index$excise_QMARK___15458(Object object) {
        this.xpreds = object;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        index$merge_one_index$excise_QMARK___15458 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new index$merge_one_index$excise_QMARK___15458$fn__15459(object), this_.xpreds);
    }
}

