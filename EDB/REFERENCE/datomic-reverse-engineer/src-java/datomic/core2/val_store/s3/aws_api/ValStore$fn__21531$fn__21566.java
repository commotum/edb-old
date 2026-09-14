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
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$G__21532__21567;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$G__21533__21569;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$G__21534__21571;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$G__21535__21573;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$G__21536__21575;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$G__21537__21577;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579;

public final class ValStore$fn__21531$fn__21566
extends AFunction {
    Object _;
    Object bucket;
    Object prefix;
    Object c__10230__auto__;
    Object opts;
    Object client;
    Object captured_bindings__10231__auto__;
    Object k;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStore$fn__21531$fn__21566(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this._ = object;
        this.bucket = object2;
        this.prefix = object3;
        this.c__10230__auto__ = object4;
        this.opts = object5;
        this.client = object6;
        this.captured_bindings__10231__auto__ = object7;
        this.k = object8;
    }

    public Object invoke() {
        Object state__10233__auto__21597;
        ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579 f__10232__auto__21596;
        ValStore$fn__21531$fn__21566$G__21532__21567 G__21532 = new ValStore$fn__21531$fn__21566$G__21532__21567(this_.bucket);
        ValStore$fn__21531$fn__21566$G__21533__21569 G__21533 = new ValStore$fn__21531$fn__21566$G__21533__21569(this_._);
        ValStore$fn__21531$fn__21566$G__21534__21571 G__21534 = new ValStore$fn__21531$fn__21566$G__21534__21571(this_.client);
        this_.k = null;
        ValStore$fn__21531$fn__21566$G__21535__21573 G__21535 = new ValStore$fn__21531$fn__21566$G__21535__21573(this_.k);
        ValStore$fn__21531$fn__21566$G__21536__21575 G__21536 = new ValStore$fn__21531$fn__21566$G__21536__21575(this_.prefix);
        this_.opts = null;
        ValStore$fn__21531$fn__21566$G__21537__21577 G__21537 = new ValStore$fn__21531$fn__21566$G__21537__21577(this_.opts);
        ValStore$fn__21531$fn__21566$G__21534__21571 valStore$fn__21531$fn__21566$G__21534__21571 = G__21534;
        G__21534 = null;
        ValStore$fn__21531$fn__21566$G__21535__21573 valStore$fn__21531$fn__21566$G__21535__21573 = G__21535;
        G__21535 = null;
        ValStore$fn__21531$fn__21566$G__21532__21567 valStore$fn__21531$fn__21566$G__21532__21567 = G__21532;
        G__21532 = null;
        ValStore$fn__21531$fn__21566$G__21533__21569 valStore$fn__21531$fn__21566$G__21533__21569 = G__21533;
        G__21533 = null;
        ValStore$fn__21531$fn__21566$G__21537__21577 valStore$fn__21531$fn__21566$G__21537__21577 = G__21537;
        G__21537 = null;
        ValStore$fn__21531$fn__21566$G__21536__21575 valStore$fn__21531$fn__21566$G__21536__21575 = G__21536;
        G__21536 = null;
        ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579 valStore$fn__21531$fn__21566$state_machine__9975__auto____21579 = f__10232__auto__21596 = new ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579((Object)valStore$fn__21531$fn__21566$G__21534__21571, (Object)valStore$fn__21531$fn__21566$G__21535__21573, (Object)valStore$fn__21531$fn__21566$G__21532__21567, (Object)valStore$fn__21531$fn__21566$G__21533__21569, (Object)valStore$fn__21531$fn__21566$G__21537__21577, (Object)valStore$fn__21531$fn__21566$G__21536__21575);
        f__10232__auto__21596 = null;
        Object statearr_21594 = ((IFn)valStore$fn__21531$fn__21566$state_machine__9975__auto____21579).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21594, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21594, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_21594;
        statearr_21594 = null;
        Object object2 = state__10233__auto__21597 = object;
        state__10233__auto__21597 = null;
        ValStore$fn__21531$fn__21566 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

