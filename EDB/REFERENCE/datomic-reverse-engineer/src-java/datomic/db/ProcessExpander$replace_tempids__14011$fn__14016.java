/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class ProcessExpander$replace_tempids__14011$fn__14016
extends AFunction {
    Object v;
    Object tempid_at_index_QMARK_;

    public ProcessExpander$replace_tempids__14011$fn__14016(Object object, Object object2) {
        this.v = object;
        this.tempid_at_index_QMARK_ = object2;
    }

    public Object invoke() {
        Boolean bl;
        long n = 0L;
        while (true) {
            if (n == (long)RT.count((Object)this.v)) {
                bl = Boolean.FALSE;
                break;
            }
            Object object = ((IFn)this.tempid_at_index_QMARK_).invoke((Object)Numbers.num((long)n), RT.get((Object)this.v, (Object)Numbers.num((long)n)));
            if (object != null && object != Boolean.FALSE) {
                bl = Boolean.TRUE;
                break;
            }
            ++n;
        }
        return bl;
    }
}

