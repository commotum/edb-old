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
import datomic.core2.log$ensure_tombstone$fn__20596$G__20565__20597;
import datomic.core2.log$ensure_tombstone$fn__20596$G__20566__20599;
import datomic.core2.log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601;

public final class log$ensure_tombstone$fn__20596
extends AFunction {
    Object tombstone;
    Object log;
    Object c__10230__auto__;
    Object captured_bindings__10231__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public log$ensure_tombstone$fn__20596(Object object, Object object2, Object object3, Object object4) {
        this.tombstone = object;
        this.log = object2;
        this.c__10230__auto__ = object3;
        this.captured_bindings__10231__auto__ = object4;
    }

    public Object invoke() {
        Object state__10233__auto__20630;
        log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601 f__10232__auto__20629;
        log$ensure_tombstone$fn__20596$G__20566__20599 G__20566;
        this_.log = null;
        log$ensure_tombstone$fn__20596$G__20565__20597 G__20565 = new log$ensure_tombstone$fn__20596$G__20565__20597(this_.log);
        this_.tombstone = null;
        log$ensure_tombstone$fn__20596$G__20566__20599 log$ensure_tombstone$fn__20596$G__20566__20599 = G__20566 = new log$ensure_tombstone$fn__20596$G__20566__20599(this_.tombstone);
        G__20566 = null;
        log$ensure_tombstone$fn__20596$G__20565__20597 log$ensure_tombstone$fn__20596$G__20565__20597 = G__20565;
        G__20565 = null;
        log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601 log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601 = f__10232__auto__20629 = new log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601((Object)log$ensure_tombstone$fn__20596$G__20566__20599, (Object)log$ensure_tombstone$fn__20596$G__20565__20597);
        f__10232__auto__20629 = null;
        Object statearr_20627 = ((IFn)log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20627, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20627, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20627;
        statearr_20627 = null;
        Object object2 = state__10233__auto__20630 = object;
        state__10233__auto__20630 = null;
        log$ensure_tombstone$fn__20596 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

