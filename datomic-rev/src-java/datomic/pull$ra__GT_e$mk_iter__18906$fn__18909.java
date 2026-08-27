/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Util;
import datomic.Datom;

public final class pull$ra__GT_e$mk_iter__18906$fn__18909
extends AFunction {
    Object rid;
    Object attrid;

    public pull$ra__GT_e$mk_iter__18906$fn__18909(Object object, Object object2) {
        this.rid = object;
        this.attrid = object2;
    }

    public Object invoke(Object p1__18905_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__18911 = Util.equiv((Object)this_.rid, (Object)((Datom)p1__18905_SHARP_).v());
        if (and__5236__auto__18911) {
            Object object = p1__18905_SHARP_;
            p1__18905_SHARP_ = null;
            pull$ra__GT_e$mk_iter__18906$fn__18909 this_ = null;
            bl = Util.equiv((Object)this_.attrid, (Object)((Datom)object).a()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__18911 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

