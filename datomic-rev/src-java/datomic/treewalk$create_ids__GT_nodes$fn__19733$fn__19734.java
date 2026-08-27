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

public final class treewalk$create_ids__GT_nodes$fn__19733$fn__19734
extends AFunction {
    Object allow_missing_QMARK_;
    Object lookup;
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"create-node");

    public treewalk$create_ids__GT_nodes$fn__19733$fn__19734(Object object, Object object2) {
        this.allow_missing_QMARK_ = object;
        this.lookup = object2;
    }

    public Object invoke(Object p1__19732_SHARP_) {
        Object object = p1__19732_SHARP_;
        p1__19732_SHARP_ = null;
        treewalk$create_ids__GT_nodes$fn__19733$fn__19734 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.lookup, this_.allow_missing_QMARK_);
    }
}

