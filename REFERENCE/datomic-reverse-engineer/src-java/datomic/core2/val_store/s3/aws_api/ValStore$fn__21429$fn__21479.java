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
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21430__21480;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21431__21482;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21432__21484;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21433__21486;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21434__21488;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21435__21490;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21436__21492;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21437__21494;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21438__21496;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$G__21439__21498;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500;

public final class ValStore$fn__21429$fn__21479
extends AFunction {
    Object c__10230__auto__;
    Object k;
    Object bucket;
    Object p__21426;
    Object map__21428;
    Object captured_bindings__10231__auto__;
    Object _;
    Object prefix;
    Object client;
    Object v;
    Object opts;
    Object val;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStore$fn__21429$fn__21479(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12) {
        this.c__10230__auto__ = object;
        this.k = object2;
        this.bucket = object3;
        this.p__21426 = object4;
        this.map__21428 = object5;
        this.captured_bindings__10231__auto__ = object6;
        this._ = object7;
        this.prefix = object8;
        this.client = object9;
        this.v = object10;
        this.opts = object11;
        this.val = object12;
    }

    public Object invoke() {
        Object state__10233__auto__21527;
        ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500 f__10232__auto__21526;
        ValStore$fn__21429$fn__21479$G__21430__21480 G__21430 = new ValStore$fn__21429$fn__21479$G__21430__21480(this_.bucket);
        this_.map__21428 = null;
        ValStore$fn__21429$fn__21479$G__21431__21482 G__21431 = new ValStore$fn__21429$fn__21479$G__21431__21482(this_.map__21428);
        this_.v = null;
        ValStore$fn__21429$fn__21479$G__21432__21484 G__21432 = new ValStore$fn__21429$fn__21479$G__21432__21484(this_.v);
        this_.p__21426 = null;
        ValStore$fn__21429$fn__21479$G__21433__21486 G__21433 = new ValStore$fn__21429$fn__21479$G__21433__21486(this_.p__21426);
        ValStore$fn__21429$fn__21479$G__21434__21488 G__21434 = new ValStore$fn__21429$fn__21479$G__21434__21488(this_._);
        ValStore$fn__21429$fn__21479$G__21435__21490 G__21435 = new ValStore$fn__21429$fn__21479$G__21435__21490(this_.client);
        this_.val = null;
        ValStore$fn__21429$fn__21479$G__21436__21492 G__21436 = new ValStore$fn__21429$fn__21479$G__21436__21492(this_.val);
        this_.k = null;
        ValStore$fn__21429$fn__21479$G__21437__21494 G__21437 = new ValStore$fn__21429$fn__21479$G__21437__21494(this_.k);
        ValStore$fn__21429$fn__21479$G__21438__21496 G__21438 = new ValStore$fn__21429$fn__21479$G__21438__21496(this_.prefix);
        this_.opts = null;
        ValStore$fn__21429$fn__21479$G__21439__21498 G__21439 = new ValStore$fn__21429$fn__21479$G__21439__21498(this_.opts);
        ValStore$fn__21429$fn__21479$G__21430__21480 valStore$fn__21429$fn__21479$G__21430__21480 = G__21430;
        G__21430 = null;
        ValStore$fn__21429$fn__21479$G__21431__21482 valStore$fn__21429$fn__21479$G__21431__21482 = G__21431;
        G__21431 = null;
        ValStore$fn__21429$fn__21479$G__21433__21486 valStore$fn__21429$fn__21479$G__21433__21486 = G__21433;
        G__21433 = null;
        ValStore$fn__21429$fn__21479$G__21439__21498 valStore$fn__21429$fn__21479$G__21439__21498 = G__21439;
        G__21439 = null;
        ValStore$fn__21429$fn__21479$G__21436__21492 valStore$fn__21429$fn__21479$G__21436__21492 = G__21436;
        G__21436 = null;
        ValStore$fn__21429$fn__21479$G__21434__21488 valStore$fn__21429$fn__21479$G__21434__21488 = G__21434;
        G__21434 = null;
        ValStore$fn__21429$fn__21479$G__21437__21494 valStore$fn__21429$fn__21479$G__21437__21494 = G__21437;
        G__21437 = null;
        ValStore$fn__21429$fn__21479$G__21438__21496 valStore$fn__21429$fn__21479$G__21438__21496 = G__21438;
        G__21438 = null;
        ValStore$fn__21429$fn__21479$G__21435__21490 valStore$fn__21429$fn__21479$G__21435__21490 = G__21435;
        G__21435 = null;
        ValStore$fn__21429$fn__21479$G__21432__21484 valStore$fn__21429$fn__21479$G__21432__21484 = G__21432;
        G__21432 = null;
        ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500 valStore$fn__21429$fn__21479$state_machine__9975__auto____21500 = f__10232__auto__21526 = new ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500((Object)valStore$fn__21429$fn__21479$G__21430__21480, (Object)valStore$fn__21429$fn__21479$G__21431__21482, (Object)valStore$fn__21429$fn__21479$G__21433__21486, (Object)valStore$fn__21429$fn__21479$G__21439__21498, (Object)valStore$fn__21429$fn__21479$G__21436__21492, (Object)valStore$fn__21429$fn__21479$G__21434__21488, (Object)valStore$fn__21429$fn__21479$G__21437__21494, (Object)valStore$fn__21429$fn__21479$G__21438__21496, (Object)valStore$fn__21429$fn__21479$G__21435__21490, (Object)valStore$fn__21429$fn__21479$G__21432__21484);
        f__10232__auto__21526 = null;
        Object statearr_21524 = ((IFn)valStore$fn__21429$fn__21479$state_machine__9975__auto____21500).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21524, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21524, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_21524;
        statearr_21524 = null;
        Object object2 = state__10233__auto__21527 = object;
        state__10233__auto__21527 = null;
        ValStore$fn__21429$fn__21479 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

