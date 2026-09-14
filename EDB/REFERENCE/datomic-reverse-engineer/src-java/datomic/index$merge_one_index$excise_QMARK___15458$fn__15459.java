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

public final class index$merge_one_index$excise_QMARK___15458$fn__15459
extends AFunction {
    Object d;
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"remove?");

    public index$merge_one_index$excise_QMARK___15458$fn__15459(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__15442_SHARP_) {
        Object object = p1__15442_SHARP_;
        p1__15442_SHARP_ = null;
        index$merge_one_index$excise_QMARK___15458$fn__15459 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.d);
    }
}

