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

public final class pull$ea__GT_v$mk_iter__18919$fn__18922
extends AFunction {
    Object attrid;
    Object eid;

    public pull$ea__GT_v$mk_iter__18919$fn__18922(Object object, Object object2) {
        this.attrid = object;
        this.eid = object2;
    }

    public Object invoke(Object p1__18918_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__18924 = Util.equiv((Object)this_.eid, (Object)((Datom)p1__18918_SHARP_).e());
        if (and__5236__auto__18924) {
            Object object = p1__18918_SHARP_;
            p1__18918_SHARP_ = null;
            pull$ea__GT_v$mk_iter__18919$fn__18922 this_ = null;
            bl = Util.equiv((Object)this_.attrid, (Object)((Datom)object).a()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__18924 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

