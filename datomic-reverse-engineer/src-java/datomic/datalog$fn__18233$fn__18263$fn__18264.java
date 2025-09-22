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
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18263$fn__18264
extends AFunction {
    Object d;

    public datalog$fn__18233$fn__18263$fn__18264(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__18219_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__18266 = Util.equiv((long)((IDatum)this_.d).getA(), (long)((IDatum)p1__18219_SHARP_).getA());
        if (and__5236__auto__18266) {
            Object object = p1__18219_SHARP_;
            p1__18219_SHARP_ = null;
            datalog$fn__18233$fn__18263$fn__18264 this_ = null;
            bl = Util.equiv((Object)((IDatum)this_.d).getV(), (Object)((IDatum)object).getV()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__18266 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

