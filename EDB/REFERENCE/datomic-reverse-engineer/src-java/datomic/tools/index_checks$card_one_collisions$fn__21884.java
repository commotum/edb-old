/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;

public final class index_checks$card_one_collisions$fn__21884
extends AFunction {
    Object progress;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");

    public index_checks$card_one_collisions$fn__21884(Object object) {
        this.progress = object;
    }

    public Object invoke(Object d1, Object d2) {
        Boolean bl;
        ((IFn)this_.progress).invoke();
        IFn iFn = (IFn)const__0.getRawRoot();
        boolean and__5236__auto__21886 = Util.equiv((Object)((Datom)d1).e(), (Object)((Datom)d2).e());
        if (and__5236__auto__21886) {
            Object object = d1;
            d1 = null;
            Object object2 = d2;
            d2 = null;
            bl = Util.equiv((Object)((Datom)object).a(), (Object)((Datom)object2).a()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__21886 ? Boolean.TRUE : Boolean.FALSE;
        }
        index_checks$card_one_collisions$fn__21884 this_ = null;
        return iFn.invoke((Object)bl);
    }
}

