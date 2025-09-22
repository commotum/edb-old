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
import datomic.impl.db.IDatum;

public final class db$accept_index$fn__13644$fn__13656
extends AFunction {
    Object ft_basis;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"fulltext?");

    public db$accept_index$fn__13644$fn__13656(Object object) {
        this.ft_basis = object;
    }

    public Object invoke(Object p1__13638_SHARP_) {
        Object object = p1__13638_SHARP_;
        p1__13638_SHARP_ = null;
        db$accept_index$fn__13644$fn__13656 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.ft_basis, (Object)((IDatum)object).getA());
    }
}

