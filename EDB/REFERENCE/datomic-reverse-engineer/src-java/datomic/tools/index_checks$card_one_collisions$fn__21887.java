/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Datom;

public final class index_checks$card_one_collisions$fn__21887
extends AFunction {
    Object as;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public index_checks$card_one_collisions$fn__21887(Object object) {
        this.as = object;
    }

    public Object invoke(Object p1__21883_SHARP_) {
        Object object = p1__21883_SHARP_;
        p1__21883_SHARP_ = null;
        index_checks$card_one_collisions$fn__21887 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.as, ((Datom)object).a());
    }
}

