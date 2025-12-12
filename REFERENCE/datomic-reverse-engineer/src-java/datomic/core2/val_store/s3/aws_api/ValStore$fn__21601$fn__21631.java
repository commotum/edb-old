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
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$G__21602__21632;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$G__21603__21634;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$G__21604__21636;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$G__21605__21638;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$G__21606__21640;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$G__21607__21642;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644;

public final class ValStore$fn__21601$fn__21631
extends AFunction {
    Object bucket;
    Object c__10230__auto__;
    Object _;
    Object prefix;
    Object opts;
    Object client;
    Object k;
    Object captured_bindings__10231__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStore$fn__21601$fn__21631(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.bucket = object;
        this.c__10230__auto__ = object2;
        this._ = object3;
        this.prefix = object4;
        this.opts = object5;
        this.client = object6;
        this.k = object7;
        this.captured_bindings__10231__auto__ = object8;
    }

    public Object invoke() {
        Object state__10233__auto__21661;
        ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644 f__10232__auto__21660;
        ValStore$fn__21601$fn__21631$G__21602__21632 G__21602 = new ValStore$fn__21601$fn__21631$G__21602__21632(this_.bucket);
        ValStore$fn__21601$fn__21631$G__21603__21634 G__21603 = new ValStore$fn__21601$fn__21631$G__21603__21634(this_._);
        ValStore$fn__21601$fn__21631$G__21604__21636 G__21604 = new ValStore$fn__21601$fn__21631$G__21604__21636(this_.client);
        this_.k = null;
        ValStore$fn__21601$fn__21631$G__21605__21638 G__21605 = new ValStore$fn__21601$fn__21631$G__21605__21638(this_.k);
        ValStore$fn__21601$fn__21631$G__21606__21640 G__21606 = new ValStore$fn__21601$fn__21631$G__21606__21640(this_.prefix);
        this_.opts = null;
        ValStore$fn__21601$fn__21631$G__21607__21642 G__21607 = new ValStore$fn__21601$fn__21631$G__21607__21642(this_.opts);
        ValStore$fn__21601$fn__21631$G__21602__21632 valStore$fn__21601$fn__21631$G__21602__21632 = G__21602;
        G__21602 = null;
        ValStore$fn__21601$fn__21631$G__21604__21636 valStore$fn__21601$fn__21631$G__21604__21636 = G__21604;
        G__21604 = null;
        ValStore$fn__21601$fn__21631$G__21607__21642 valStore$fn__21601$fn__21631$G__21607__21642 = G__21607;
        G__21607 = null;
        ValStore$fn__21601$fn__21631$G__21606__21640 valStore$fn__21601$fn__21631$G__21606__21640 = G__21606;
        G__21606 = null;
        ValStore$fn__21601$fn__21631$G__21603__21634 valStore$fn__21601$fn__21631$G__21603__21634 = G__21603;
        G__21603 = null;
        ValStore$fn__21601$fn__21631$G__21605__21638 valStore$fn__21601$fn__21631$G__21605__21638 = G__21605;
        G__21605 = null;
        ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644 valStore$fn__21601$fn__21631$state_machine__9975__auto____21644 = f__10232__auto__21660 = new ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644((Object)valStore$fn__21601$fn__21631$G__21602__21632, (Object)valStore$fn__21601$fn__21631$G__21604__21636, (Object)valStore$fn__21601$fn__21631$G__21607__21642, (Object)valStore$fn__21601$fn__21631$G__21606__21640, (Object)valStore$fn__21601$fn__21631$G__21603__21634, (Object)valStore$fn__21601$fn__21631$G__21605__21638);
        f__10232__auto__21660 = null;
        Object statearr_21658 = ((IFn)valStore$fn__21601$fn__21631$state_machine__9975__auto____21644).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21658, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21658, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_21658;
        statearr_21658 = null;
        Object object2 = state__10233__auto__21661 = object;
        state__10233__auto__21661 = null;
        ValStore$fn__21601$fn__21631 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

