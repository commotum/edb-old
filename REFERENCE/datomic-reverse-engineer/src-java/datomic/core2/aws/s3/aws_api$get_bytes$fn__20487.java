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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$G__20462__20488;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$G__20463__20490;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$G__20464__20492;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$G__20465__20494;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$G__20466__20496;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498;

public final class aws_api$get_bytes$fn__20487
extends AFunction {
    Object map__20461;
    Object captured_bindings__10231__auto__;
    Object p__20460;
    Object c__10230__auto__;
    Object key;
    Object bucket;
    Object client;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public aws_api$get_bytes$fn__20487(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.map__20461 = object;
        this.captured_bindings__10231__auto__ = object2;
        this.p__20460 = object3;
        this.c__10230__auto__ = object4;
        this.key = object5;
        this.bucket = object6;
        this.client = object7;
    }

    public Object invoke() {
        Object state__10233__auto__20518;
        aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498 f__10232__auto__20517;
        this_.p__20460 = null;
        aws_api$get_bytes$fn__20487$G__20462__20488 G__20462 = new aws_api$get_bytes$fn__20487$G__20462__20488(this_.p__20460);
        this_.map__20461 = null;
        aws_api$get_bytes$fn__20487$G__20463__20490 G__20463 = new aws_api$get_bytes$fn__20487$G__20463__20490(this_.map__20461);
        this_.bucket = null;
        aws_api$get_bytes$fn__20487$G__20464__20492 G__20464 = new aws_api$get_bytes$fn__20487$G__20464__20492(this_.bucket);
        this_.client = null;
        aws_api$get_bytes$fn__20487$G__20465__20494 G__20465 = new aws_api$get_bytes$fn__20487$G__20465__20494(this_.client);
        this_.key = null;
        aws_api$get_bytes$fn__20487$G__20466__20496 G__20466 = new aws_api$get_bytes$fn__20487$G__20466__20496(this_.key);
        aws_api$get_bytes$fn__20487$G__20462__20488 aws_api$get_bytes$fn__20487$G__20462__20488 = G__20462;
        G__20462 = null;
        aws_api$get_bytes$fn__20487$G__20464__20492 aws_api$get_bytes$fn__20487$G__20464__20492 = G__20464;
        G__20464 = null;
        aws_api$get_bytes$fn__20487$G__20463__20490 aws_api$get_bytes$fn__20487$G__20463__20490 = G__20463;
        G__20463 = null;
        aws_api$get_bytes$fn__20487$G__20465__20494 aws_api$get_bytes$fn__20487$G__20465__20494 = G__20465;
        G__20465 = null;
        aws_api$get_bytes$fn__20487$G__20466__20496 aws_api$get_bytes$fn__20487$G__20466__20496 = G__20466;
        G__20466 = null;
        aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498 aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498 = f__10232__auto__20517 = new aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498((Object)aws_api$get_bytes$fn__20487$G__20462__20488, (Object)aws_api$get_bytes$fn__20487$G__20464__20492, (Object)aws_api$get_bytes$fn__20487$G__20463__20490, (Object)aws_api$get_bytes$fn__20487$G__20465__20494, (Object)aws_api$get_bytes$fn__20487$G__20466__20496);
        f__10232__auto__20517 = null;
        Object statearr_20515 = ((IFn)aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20515, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20515, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20515;
        statearr_20515 = null;
        Object object2 = state__10233__auto__20518 = object;
        state__10233__auto__20518 = null;
        aws_api$get_bytes$fn__20487 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

