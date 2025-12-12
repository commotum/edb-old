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
import datomic.core2.async$put_all_BANG_$fn__19616$G__19583__19617;
import datomic.core2.async$put_all_BANG_$fn__19616$G__19584__19619;
import datomic.core2.async$put_all_BANG_$fn__19616$G__19585__19621;
import datomic.core2.async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623;

public final class async$put_all_BANG_$fn__19616
extends AFunction {
    Object close_QMARK_;
    Object captured_bindings__10231__auto__;
    Object coll;
    Object c__10230__auto__;
    Object ch;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public async$put_all_BANG_$fn__19616(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.close_QMARK_ = object;
        this.captured_bindings__10231__auto__ = object2;
        this.coll = object3;
        this.c__10230__auto__ = object4;
        this.ch = object5;
    }

    public Object invoke() {
        Object state__10233__auto__19649;
        async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623 f__10232__auto__19648;
        this_.ch = null;
        async$put_all_BANG_$fn__19616$G__19583__19617 G__19583 = new async$put_all_BANG_$fn__19616$G__19583__19617(this_.ch);
        this_.coll = null;
        async$put_all_BANG_$fn__19616$G__19584__19619 G__19584 = new async$put_all_BANG_$fn__19616$G__19584__19619(this_.coll);
        this_.close_QMARK_ = null;
        async$put_all_BANG_$fn__19616$G__19585__19621 G__19585 = new async$put_all_BANG_$fn__19616$G__19585__19621(this_.close_QMARK_);
        async$put_all_BANG_$fn__19616$G__19584__19619 async$put_all_BANG_$fn__19616$G__19584__19619 = G__19584;
        G__19584 = null;
        async$put_all_BANG_$fn__19616$G__19585__19621 async$put_all_BANG_$fn__19616$G__19585__19621 = G__19585;
        G__19585 = null;
        async$put_all_BANG_$fn__19616$G__19583__19617 async$put_all_BANG_$fn__19616$G__19583__19617 = G__19583;
        G__19583 = null;
        async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623 async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623 = f__10232__auto__19648 = new async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623((Object)async$put_all_BANG_$fn__19616$G__19584__19619, (Object)async$put_all_BANG_$fn__19616$G__19585__19621, (Object)async$put_all_BANG_$fn__19616$G__19583__19617);
        f__10232__auto__19648 = null;
        Object statearr_19646 = ((IFn)async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19646, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19646, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_19646;
        statearr_19646 = null;
        Object object2 = state__10233__auto__19649 = object;
        state__10233__auto__19649 = null;
        async$put_all_BANG_$fn__19616 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

