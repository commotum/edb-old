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
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.async$retry$fn__19535$G__19480__19536;
import datomic.core2.async$retry$fn__19535$G__19481__19538;
import datomic.core2.async$retry$fn__19535$G__19482__19540;
import datomic.core2.async$retry$fn__19535$G__19483__19542;
import datomic.core2.async$retry$fn__19535$G__19484__19544;
import datomic.core2.async$retry$fn__19535$G__19485__19546;
import datomic.core2.async$retry$fn__19535$G__19486__19548;
import datomic.core2.async$retry$fn__19535$state_machine__9975__auto____19550;

public final class async$retry$fn__19535
extends AFunction {
    Object backoff;
    Object fail;
    Object f;
    Object ch;
    Object c__10230__auto__;
    Object captured_bindings__10231__auto__;
    Object pred;
    Object p__19478;
    Object map__19479;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public async$retry$fn__19535(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.backoff = object;
        this.fail = object2;
        this.f = object3;
        this.ch = object4;
        this.c__10230__auto__ = object5;
        this.captured_bindings__10231__auto__ = object6;
        this.pred = object7;
        this.p__19478 = object8;
        this.map__19479 = object9;
    }

    public Object invoke() {
        Object state__10233__auto__19579;
        async$retry$fn__19535$state_machine__9975__auto____19550 f__10232__auto__19578;
        this_.p__19478 = null;
        async$retry$fn__19535$G__19480__19536 G__19480 = new async$retry$fn__19535$G__19480__19536(this_.p__19478);
        this_.map__19479 = null;
        async$retry$fn__19535$G__19481__19538 G__19481 = new async$retry$fn__19535$G__19481__19538(this_.map__19479);
        this_.f = null;
        async$retry$fn__19535$G__19482__19540 G__19482 = new async$retry$fn__19535$G__19482__19540(this_.f);
        this_.pred = null;
        async$retry$fn__19535$G__19483__19542 G__19483 = new async$retry$fn__19535$G__19483__19542(this_.pred);
        this_.backoff = null;
        async$retry$fn__19535$G__19484__19544 G__19484 = new async$retry$fn__19535$G__19484__19544(this_.backoff);
        this_.ch = null;
        async$retry$fn__19535$G__19485__19546 G__19485 = new async$retry$fn__19535$G__19485__19546(this_.ch);
        this_.fail = null;
        async$retry$fn__19535$G__19486__19548 G__19486 = new async$retry$fn__19535$G__19486__19548(this_.fail);
        async$retry$fn__19535$G__19482__19540 async$retry$fn__19535$G__19482__19540 = G__19482;
        G__19482 = null;
        async$retry$fn__19535$G__19480__19536 async$retry$fn__19535$G__19480__19536 = G__19480;
        G__19480 = null;
        async$retry$fn__19535$G__19485__19546 async$retry$fn__19535$G__19485__19546 = G__19485;
        G__19485 = null;
        async$retry$fn__19535$G__19481__19538 async$retry$fn__19535$G__19481__19538 = G__19481;
        G__19481 = null;
        async$retry$fn__19535$G__19483__19542 async$retry$fn__19535$G__19483__19542 = G__19483;
        G__19483 = null;
        async$retry$fn__19535$G__19484__19544 async$retry$fn__19535$G__19484__19544 = G__19484;
        G__19484 = null;
        async$retry$fn__19535$G__19486__19548 async$retry$fn__19535$G__19486__19548 = G__19486;
        G__19486 = null;
        async$retry$fn__19535$state_machine__9975__auto____19550 async$retry$fn__19535$state_machine__9975__auto____19550 = f__10232__auto__19578 = new async$retry$fn__19535$state_machine__9975__auto____19550((Object)async$retry$fn__19535$G__19482__19540, (Object)async$retry$fn__19535$G__19480__19536, (Object)async$retry$fn__19535$G__19485__19546, (Object)async$retry$fn__19535$G__19481__19538, (Object)async$retry$fn__19535$G__19483__19542, (Object)async$retry$fn__19535$G__19484__19544, (Object)async$retry$fn__19535$G__19486__19548);
        f__10232__auto__19578 = null;
        Object statearr_19576 = ((IFn)async$retry$fn__19535$state_machine__9975__auto____19550).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19576, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19576, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_19576;
        statearr_19576 = null;
        Object object2 = state__10233__auto__19579 = object;
        state__10233__auto__19579 = null;
        async$retry$fn__19535 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

