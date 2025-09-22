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

public final class datalog$fn__18233$fn__18306$fn__18307
extends AFunction {
    Object d;

    public datalog$fn__18233$fn__18306$fn__18307(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__18227_SHARP_) {
        Object object = p1__18227_SHARP_;
        p1__18227_SHARP_ = null;
        datalog$fn__18233$fn__18306$fn__18307 this_ = null;
        return Util.equiv((long)((IDatum)this_.d).getE(), (long)((IDatum)object).getE()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

