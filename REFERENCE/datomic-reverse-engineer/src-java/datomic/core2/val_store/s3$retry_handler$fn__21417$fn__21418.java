/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class s3$retry_handler$fn__21417$fn__21418
extends AFunction {
    Object base;
    Object backoff;
    public static final Var const__0 = RT.var((String)"datomic.core2.retry", (String)"full-jitter");
    public static final Var const__1 = RT.var((String)"datomic.core2.retry", (String)"calc-exp-backoff");

    public s3$retry_handler$fn__21417$fn__21418(Object object, Object object2) {
        this.base = object;
        this.backoff = object2;
    }

    public Object invoke(Object round_map) {
        Object object = round_map;
        round_map = null;
        s3$retry_handler$fn__21417$fn__21418 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)((IFn.LLOL)const__1.getRawRoot()).invokePrim(RT.longCast((Object)((Number)this_.backoff)), RT.longCast((Object)((Number)this_.base)), object)));
    }
}

