/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;

public final class backup$unreadable_seg_ids$unreadable_QMARK___20347
extends AFunction {
    Object lookup;

    public backup$unreadable_seg_ids$unreadable_QMARK___20347(Object object) {
        this.lookup = object;
    }

    public Object invoke(Object p1__20346_SHARP_) {
        Boolean bl;
        try {
            Object object = p1__20346_SHARP_;
            p1__20346_SHARP_ = null;
            RT.get((Object)this.lookup, (Object)object);
            bl = Boolean.FALSE;
        }
        catch (Throwable _) {
            bl = Boolean.TRUE;
        }
        return bl;
    }
}

