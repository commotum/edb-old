/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.Util;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18285$fn__18286
extends AFunction {
    Object const_attrid;
    Object whilee;

    public datalog$fn__18233$fn__18285$fn__18286(Object object, Object object2) {
        this.const_attrid = object;
        this.whilee = object2;
    }

    public Object invoke(Object p1__18224_SHARP_) {
        Object object;
        boolean and__5236__auto__18289 = Util.equiv((Object)this_.const_attrid, (long)((IDatum)p1__18224_SHARP_).getA());
        if (and__5236__auto__18289) {
            boolean or__5238__auto__18288 = Util.identical((Object)this_.whilee, null);
            if (or__5238__auto__18288) {
                object = or__5238__auto__18288 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object2 = p1__18224_SHARP_;
                p1__18224_SHARP_ = null;
                datalog$fn__18233$fn__18285$fn__18286 this_ = null;
                object = ((IFn)this_.whilee).invoke((Object)Numbers.num((long)((IDatum)object2).getE()));
            }
        } else {
            object = and__5236__auto__18289 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }
}

