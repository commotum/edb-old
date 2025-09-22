/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.Attribute;

public final class integrity$nohistory_attrs$fn__21984
extends AFunction {
    public Object invoke(Object p1__21983_SHARP_) {
        Object object = p1__21983_SHARP_;
        p1__21983_SHARP_ = null;
        return ((Attribute)object).noHistory;
    }
}

