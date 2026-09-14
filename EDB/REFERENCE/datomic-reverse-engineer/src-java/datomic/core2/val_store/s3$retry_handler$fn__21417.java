/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.s3$retry_handler$fn__21417$fn__21418;

public final class s3$retry_handler$fn__21417
extends AFunction {
    Object base;
    Object f;
    Object metric_cb;
    Object retriable_QMARK_;
    Object backoff;
    public static final Var const__0 = RT.var((String)"datomic.core2.retry", (String)"retry");
    public static final Var const__1 = RT.var((String)"datomic.core2.anomalies", (String)"ok?");
    public static final Keyword const__2 = RT.keyword(null, (String)"on-success");
    public static final Keyword const__3 = RT.keyword(null, (String)"on-failure");

    public s3$retry_handler$fn__21417(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.base = object;
        this.f = object2;
        this.metric_cb = object3;
        this.retriable_QMARK_ = object4;
        this.backoff = object5;
    }

    public Object invoke() {
        s3$retry_handler$fn__21417 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.f, const__1.getRawRoot(), this_.retriable_QMARK_, (Object)new s3$retry_handler$fn__21417$fn__21418(this_.base, this_.backoff), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, this_.metric_cb, const__3, this_.metric_cb}));
    }
}

