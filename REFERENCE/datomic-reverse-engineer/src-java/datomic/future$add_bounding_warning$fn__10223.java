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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.future$add_bounding_warning$fn__10223$G__10181__10224;
import datomic.future$add_bounding_warning$fn__10223$G__10182__10226;
import datomic.future$add_bounding_warning$fn__10223$G__10183__10228;
import datomic.future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230;

public final class future$add_bounding_warning$fn__10223
extends AFunction {
    Object c__6597__auto__;
    Object seconds;
    Object promise_ch;
    Object context;
    Object captured_bindings__6598__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public future$add_bounding_warning$fn__10223(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.c__6597__auto__ = object;
        this.seconds = object2;
        this.promise_ch = object3;
        this.context = object4;
        this.captured_bindings__6598__auto__ = object5;
    }

    public Object invoke() {
        Object state__6600__auto__10257;
        future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230 f__6599__auto__10256;
        this_.promise_ch = null;
        future$add_bounding_warning$fn__10223$G__10181__10224 G__10181 = new future$add_bounding_warning$fn__10223$G__10181__10224(this_.promise_ch);
        this_.context = null;
        future$add_bounding_warning$fn__10223$G__10182__10226 G__10182 = new future$add_bounding_warning$fn__10223$G__10182__10226(this_.context);
        this_.seconds = null;
        future$add_bounding_warning$fn__10223$G__10183__10228 G__10183 = new future$add_bounding_warning$fn__10223$G__10183__10228(this_.seconds);
        future$add_bounding_warning$fn__10223$G__10182__10226 future$add_bounding_warning$fn__10223$G__10182__10226 = G__10182;
        G__10182 = null;
        future$add_bounding_warning$fn__10223$G__10181__10224 future$add_bounding_warning$fn__10223$G__10181__10224 = G__10181;
        G__10181 = null;
        future$add_bounding_warning$fn__10223$G__10183__10228 future$add_bounding_warning$fn__10223$G__10183__10228 = G__10183;
        G__10183 = null;
        future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230 future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230 = f__6599__auto__10256 = new future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230((Object)future$add_bounding_warning$fn__10223$G__10182__10226, (Object)future$add_bounding_warning$fn__10223$G__10181__10224, (Object)future$add_bounding_warning$fn__10223$G__10183__10228);
        f__6599__auto__10256 = null;
        Object statearr_10254 = ((IFn)future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230).invoke();
        this_.c__6597__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_10254, 5L, this_.c__6597__auto__);
        this_.captured_bindings__6598__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_10254, 3L, this_.captured_bindings__6598__auto__);
        Object object = statearr_10254;
        statearr_10254 = null;
        Object object2 = state__6600__auto__10257 = object;
        state__6600__auto__10257 = null;
        future$add_bounding_warning$fn__10223 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

