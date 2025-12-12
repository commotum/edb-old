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
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631;

public final class ValStore$fn__21601
extends AFunction {
    Object bucket;
    Object _;
    Object prefix;
    Object opts;
    Object client;
    Object k;
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public ValStore$fn__21601(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.bucket = object;
        this._ = object2;
        this.prefix = object3;
        this.opts = object4;
        this.client = object5;
        this.k = object6;
    }

    public Object invoke() {
        Object captured_bindings__10231__auto__21664;
        Object c__10230__auto__21663 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        Object object = captured_bindings__10231__auto__21664 = Var.getThreadBindingFrame();
        captured_bindings__10231__auto__21664 = null;
        ((IFn)const__2.getRawRoot()).invoke((Object)new ValStore$fn__21601$fn__21631(this.bucket, c__10230__auto__21663, this._, this.prefix, this.opts, this.client, this.k, object));
        Object var1_1 = null;
        return c__10230__auto__21663;
    }
}

