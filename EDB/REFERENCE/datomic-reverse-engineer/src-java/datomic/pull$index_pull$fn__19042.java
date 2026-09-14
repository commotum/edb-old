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

public final class pull$index_pull$fn__19042
extends AFunction {
    Object attrid;

    public pull$index_pull$fn__19042(Object object) {
        this.attrid = object;
    }

    public Object invoke(Object p1__19036_SHARP_) {
        Object object = p1__19036_SHARP_;
        p1__19036_SHARP_ = null;
        pull$index_pull$fn__19042 this_ = null;
        return Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

