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

public final class pull$next_a$fn__18983
extends AFunction {
    Object e;

    public pull$next_a$fn__18983(Object object) {
        this.e = object;
    }

    public Object invoke(Object p1__18982_SHARP_) {
        Object object = p1__18982_SHARP_;
        p1__18982_SHARP_ = null;
        pull$next_a$fn__18983 this_ = null;
        return Util.equiv((Object)this_.e, (Object)((Datom)object).e()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

