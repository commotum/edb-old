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

public final class datalog$fn__18233$fn__18312$fn__18313
extends AFunction {
    Object d;

    public datalog$fn__18233$fn__18312$fn__18313(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__18228_SHARP_) {
        Object object = p1__18228_SHARP_;
        p1__18228_SHARP_ = null;
        datalog$fn__18233$fn__18312$fn__18313 this_ = null;
        return Util.equiv((Object)((IDatum)this_.d).getV(), (Object)((IDatum)object).getV()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

