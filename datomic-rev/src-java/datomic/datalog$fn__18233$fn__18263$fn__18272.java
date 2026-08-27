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

public final class datalog$fn__18233$fn__18263$fn__18272
extends AFunction {
    Object d;

    public datalog$fn__18233$fn__18263$fn__18272(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__18222_SHARP_) {
        Object object = p1__18222_SHARP_;
        p1__18222_SHARP_ = null;
        datalog$fn__18233$fn__18263$fn__18272 this_ = null;
        return Util.equiv((long)((IDatum)this_.d).getA(), (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

