/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import datomic.impl.db.IDatum;

public final class db$ident_setting_datoms$fn__13574
extends AFunction {
    public Object invoke(Object p1__13573_SHARP_) {
        Object object = p1__13573_SHARP_;
        p1__13573_SHARP_ = null;
        return Numbers.num((long)((IDatum)object).getTx());
    }
}

