/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479;

public final class ValStore$fn__21429
extends AFunction {
    Object k;
    Object bucket;
    Object p__21426;
    Object map__21428;
    Object _;
    Object prefix;
    Object client;
    Object v;
    Object opts;
    Object val;
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public ValStore$fn__21429(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.k = object;
        this.bucket = object2;
        this.p__21426 = object3;
        this.map__21428 = object4;
        this._ = object5;
        this.prefix = object6;
        this.client = object7;
        this.v = object8;
        this.opts = object9;
        this.val = object10;
    }

    public Object invoke() {
        Object captured_bindings__10231__auto__21530;
        Object c__10230__auto__21529 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object object = captured_bindings__10231__auto__21530 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__21530 = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new ValStore$fn__21429$fn__21479(c__10230__auto__21529, this.k, this.bucket, this.p__21426, this.map__21428, object, this._, this.prefix, this.client, this.v, this.opts, this.val));
        Object var1_1 = null;
        return c__10230__auto__21529;
    }
}

