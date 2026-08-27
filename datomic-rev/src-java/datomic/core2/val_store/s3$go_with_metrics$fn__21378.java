/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.s3$go_with_metrics$fn__21378$G__21352__21379;
import datomic.core2.val_store.s3$go_with_metrics$fn__21378$G__21353__21381;
import datomic.core2.val_store.s3$go_with_metrics$fn__21378$G__21354__21383;
import datomic.core2.val_store.s3$go_with_metrics$fn__21378$G__21355__21385;
import datomic.core2.val_store.s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387;

public final class s3$go_with_metrics$fn__21378
extends AFunction {
    Object op;
    Object context;
    Object c__10230__auto__;
    Object captured_bindings__10231__auto__;
    Object k;
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public s3$go_with_metrics$fn__21378(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.op = object;
        this.context = object2;
        this.c__10230__auto__ = object3;
        this.captured_bindings__10231__auto__ = object4;
        this.k = object5;
        this.f = object6;
    }

    public Object invoke() {
        Object state__10233__auto__21404;
        s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387 f__10232__auto__21403;
        s3$go_with_metrics$fn__21378$G__21355__21385 G__21355;
        this_.f = null;
        s3$go_with_metrics$fn__21378$G__21352__21379 G__21352 = new s3$go_with_metrics$fn__21378$G__21352__21379(this_.f);
        this_.k = null;
        s3$go_with_metrics$fn__21378$G__21353__21381 G__21353 = new s3$go_with_metrics$fn__21378$G__21353__21381(this_.k);
        this_.op = null;
        s3$go_with_metrics$fn__21378$G__21354__21383 G__21354 = new s3$go_with_metrics$fn__21378$G__21354__21383(this_.op);
        this_.context = null;
        s3$go_with_metrics$fn__21378$G__21355__21385 s3$go_with_metrics$fn__21378$G__21355__21385 = G__21355 = new s3$go_with_metrics$fn__21378$G__21355__21385(this_.context);
        G__21355 = null;
        s3$go_with_metrics$fn__21378$G__21353__21381 s3$go_with_metrics$fn__21378$G__21353__21381 = G__21353;
        G__21353 = null;
        s3$go_with_metrics$fn__21378$G__21354__21383 s3$go_with_metrics$fn__21378$G__21354__21383 = G__21354;
        G__21354 = null;
        s3$go_with_metrics$fn__21378$G__21352__21379 s3$go_with_metrics$fn__21378$G__21352__21379 = G__21352;
        G__21352 = null;
        s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387 s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387 = f__10232__auto__21403 = new s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387((Object)s3$go_with_metrics$fn__21378$G__21355__21385, (Object)s3$go_with_metrics$fn__21378$G__21353__21381, (Object)s3$go_with_metrics$fn__21378$G__21354__21383, (Object)s3$go_with_metrics$fn__21378$G__21352__21379);
        f__10232__auto__21403 = null;
        Object statearr_21401 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21401, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21401, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_21401;
        statearr_21401 = null;
        Object object2 = state__10233__auto__21404 = object;
        state__10233__auto__21404 = null;
        s3$go_with_metrics$fn__21378 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

