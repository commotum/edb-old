/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.Util;
import datomic.impl.db.IDatum;

public final class db$windowed$fn__12800
extends AFunction {
    Object since;
    Object asof;

    public db$windowed$fn__12800(Object object, Object object2) {
        this.since = object;
        this.asof = object2;
    }

    public Object invoke(Object d) {
        Boolean bl;
        boolean and__5236__auto__12804;
        boolean or__5238__auto__12802 = Util.identical((Object)this_.asof, null);
        boolean bl2 = and__5236__auto__12804 = or__5238__auto__12802 ? or__5238__auto__12802 : Numbers.lte((long)((IDatum)d).getT(), (Object)this_.asof);
        if (and__5236__auto__12804) {
            boolean or__5238__auto__12803 = Util.identical((Object)this_.since, null);
            if (or__5238__auto__12803) {
                bl = or__5238__auto__12803 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object = d;
                d = null;
                db$windowed$fn__12800 this_ = null;
                bl = Numbers.gt((long)((IDatum)object).getT(), (Object)this_.since) ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__12804 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

