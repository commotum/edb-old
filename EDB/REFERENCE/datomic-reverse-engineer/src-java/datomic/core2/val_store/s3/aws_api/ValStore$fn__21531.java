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
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566;

public final class ValStore$fn__21531
extends AFunction {
    Object _;
    Object bucket;
    Object prefix;
    Object opts;
    Object client;
    Object k;
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public ValStore$fn__21531(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this._ = object;
        this.bucket = object2;
        this.prefix = object3;
        this.opts = object4;
        this.client = object5;
        this.k = object6;
    }

    public Object invoke() {
        Object captured_bindings__10231__auto__21600;
        Object c__10230__auto__21599 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object object = captured_bindings__10231__auto__21600 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__21600 = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new ValStore$fn__21531$fn__21566(this._, this.bucket, this.prefix, c__10230__auto__21599, this.opts, this.client, object, this.k));
        Object var1_1 = null;
        return c__10230__auto__21599;
    }
}

