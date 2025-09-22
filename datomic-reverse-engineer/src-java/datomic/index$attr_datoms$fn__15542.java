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

public final class index$attr_datoms$fn__15542
extends AFunction {
    Object attrid;

    public index$attr_datoms$fn__15542(Object object) {
        this.attrid = object;
    }

    public Object invoke(Object p1__15541_SHARP_) {
        Object object = p1__15541_SHARP_;
        p1__15541_SHARP_ = null;
        index$attr_datoms$fn__15542 this_ = null;
        return Util.equiv((Object)this_.attrid, (long)((IDatum)object).getA()) ? Boolean.TRUE : Boolean.FALSE;
    }
}

