/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.util.ArrayList;

public final class datalog$fn__18233$fn__18332
extends AFunction {
    long sz;
    int cnt;
    Object palist;

    public datalog$fn__18233$fn__18332(long l, int n, Object object) {
        this.sz = l;
        this.cnt = n;
        this.palist = object;
    }

    public Object invoke(Object p1__18229_SHARP_) {
        int n = RT.uncheckedIntCast((Object)Numbers.unchecked_multiply((long)this.sz, (Object)p1__18229_SHARP_));
        Object object = p1__18229_SHARP_;
        p1__18229_SHARP_ = null;
        return ((ArrayList)this.palist).subList(n, RT.uncheckedIntCast((Object)((Number)Numbers.min((long)this.cnt, (Object)Numbers.unchecked_add((long)this.sz, (Object)Numbers.unchecked_multiply((long)this.sz, (Object)object))))));
    }
}

