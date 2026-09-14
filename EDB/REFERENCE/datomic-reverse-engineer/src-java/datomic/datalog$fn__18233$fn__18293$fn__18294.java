/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Util;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18293$fn__18294
extends AFunction {
    Object const_attrid;
    Object whilev;

    public datalog$fn__18233$fn__18293$fn__18294(Object object, Object object2) {
        this.const_attrid = object;
        this.whilev = object2;
    }

    public Object invoke(Object p1__18225_SHARP_) {
        Object object;
        boolean and__5236__auto__18296 = Util.equiv((Object)this_.const_attrid, (long)((IDatum)p1__18225_SHARP_).getA());
        if (and__5236__auto__18296) {
            Object object2 = p1__18225_SHARP_;
            p1__18225_SHARP_ = null;
            datalog$fn__18233$fn__18293$fn__18294 this_ = null;
            object = ((IFn)this_.whilev).invoke(((IDatum)object2).getV());
        } else {
            object = and__5236__auto__18296 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }
}

