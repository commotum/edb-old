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
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20198__20266;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20199__20268;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20200__20270;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20201__20272;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20202__20274;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20203__20276;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20204__20278;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20205__20280;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20206__20282;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20207__20284;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20208__20286;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20209__20288;
import datomic.core2.atom.logged$create_STAR_$fn__20265$G__20210__20290;
import datomic.core2.atom.logged$create_STAR_$fn__20265$state_machine__9975__auto____20292;

public final class logged$create_STAR_$fn__20265
extends AFunction {
    Object state;
    Object watches_ref;
    Object logged_atom;
    Object captured_bindings__10231__auto__;
    Object close_ch;
    Object p__20182;
    Object serialize;
    Object c__10230__auto__;
    Object validator;
    Object deserialize;
    Object log;
    Object refresh_msec;
    Object state_ref;
    Object map__20183;
    Object validator_ref;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public logged$create_STAR_$fn__20265(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15) {
        this.state = object;
        this.watches_ref = object2;
        this.logged_atom = object3;
        this.captured_bindings__10231__auto__ = object4;
        this.close_ch = object5;
        this.p__20182 = object6;
        this.serialize = object7;
        this.c__10230__auto__ = object8;
        this.validator = object9;
        this.deserialize = object10;
        this.log = object11;
        this.refresh_msec = object12;
        this.state_ref = object13;
        this.map__20183 = object14;
        this.validator_ref = object15;
    }

    public Object invoke() {
        Object state__10233__auto__20316;
        logged$create_STAR_$fn__20265$state_machine__9975__auto____20292 f__10232__auto__20315;
        logged$create_STAR_$fn__20265$G__20210__20290 G__20210;
        this_.map__20183 = null;
        logged$create_STAR_$fn__20265$G__20198__20266 G__20198 = new logged$create_STAR_$fn__20265$G__20198__20266(this_.map__20183);
        this_.log = null;
        logged$create_STAR_$fn__20265$G__20199__20268 G__20199 = new logged$create_STAR_$fn__20265$G__20199__20268(this_.log);
        this_.refresh_msec = null;
        logged$create_STAR_$fn__20265$G__20200__20270 G__20200 = new logged$create_STAR_$fn__20265$G__20200__20270(this_.refresh_msec);
        this_.serialize = null;
        logged$create_STAR_$fn__20265$G__20201__20272 G__20201 = new logged$create_STAR_$fn__20265$G__20201__20272(this_.serialize);
        this_.p__20182 = null;
        logged$create_STAR_$fn__20265$G__20202__20274 G__20202 = new logged$create_STAR_$fn__20265$G__20202__20274(this_.p__20182);
        this_.validator = null;
        logged$create_STAR_$fn__20265$G__20203__20276 G__20203 = new logged$create_STAR_$fn__20265$G__20203__20276(this_.validator);
        this_.close_ch = null;
        logged$create_STAR_$fn__20265$G__20204__20278 G__20204 = new logged$create_STAR_$fn__20265$G__20204__20278(this_.close_ch);
        this_.state_ref = null;
        logged$create_STAR_$fn__20265$G__20205__20280 G__20205 = new logged$create_STAR_$fn__20265$G__20205__20280(this_.state_ref);
        this_.logged_atom = null;
        logged$create_STAR_$fn__20265$G__20206__20282 G__20206 = new logged$create_STAR_$fn__20265$G__20206__20282(this_.logged_atom);
        this_.state = null;
        logged$create_STAR_$fn__20265$G__20207__20284 G__20207 = new logged$create_STAR_$fn__20265$G__20207__20284(this_.state);
        this_.validator_ref = null;
        logged$create_STAR_$fn__20265$G__20208__20286 G__20208 = new logged$create_STAR_$fn__20265$G__20208__20286(this_.validator_ref);
        this_.deserialize = null;
        logged$create_STAR_$fn__20265$G__20209__20288 G__20209 = new logged$create_STAR_$fn__20265$G__20209__20288(this_.deserialize);
        this_.watches_ref = null;
        logged$create_STAR_$fn__20265$G__20210__20290 logged$create_STAR_$fn__20265$G__20210__20290 = G__20210 = new logged$create_STAR_$fn__20265$G__20210__20290(this_.watches_ref);
        G__20210 = null;
        logged$create_STAR_$fn__20265$G__20207__20284 logged$create_STAR_$fn__20265$G__20207__20284 = G__20207;
        G__20207 = null;
        logged$create_STAR_$fn__20265$G__20200__20270 logged$create_STAR_$fn__20265$G__20200__20270 = G__20200;
        G__20200 = null;
        logged$create_STAR_$fn__20265$G__20209__20288 logged$create_STAR_$fn__20265$G__20209__20288 = G__20209;
        G__20209 = null;
        logged$create_STAR_$fn__20265$G__20208__20286 logged$create_STAR_$fn__20265$G__20208__20286 = G__20208;
        G__20208 = null;
        logged$create_STAR_$fn__20265$G__20198__20266 logged$create_STAR_$fn__20265$G__20198__20266 = G__20198;
        G__20198 = null;
        logged$create_STAR_$fn__20265$G__20203__20276 logged$create_STAR_$fn__20265$G__20203__20276 = G__20203;
        G__20203 = null;
        logged$create_STAR_$fn__20265$G__20199__20268 logged$create_STAR_$fn__20265$G__20199__20268 = G__20199;
        G__20199 = null;
        logged$create_STAR_$fn__20265$G__20206__20282 logged$create_STAR_$fn__20265$G__20206__20282 = G__20206;
        G__20206 = null;
        logged$create_STAR_$fn__20265$G__20205__20280 logged$create_STAR_$fn__20265$G__20205__20280 = G__20205;
        G__20205 = null;
        logged$create_STAR_$fn__20265$G__20204__20278 logged$create_STAR_$fn__20265$G__20204__20278 = G__20204;
        G__20204 = null;
        logged$create_STAR_$fn__20265$G__20201__20272 logged$create_STAR_$fn__20265$G__20201__20272 = G__20201;
        G__20201 = null;
        logged$create_STAR_$fn__20265$G__20202__20274 logged$create_STAR_$fn__20265$G__20202__20274 = G__20202;
        G__20202 = null;
        logged$create_STAR_$fn__20265$state_machine__9975__auto____20292 logged$create_STAR_$fn__20265$state_machine__9975__auto____20292 = f__10232__auto__20315 = new logged$create_STAR_$fn__20265$state_machine__9975__auto____20292((Object)logged$create_STAR_$fn__20265$G__20210__20290, (Object)logged$create_STAR_$fn__20265$G__20207__20284, (Object)logged$create_STAR_$fn__20265$G__20200__20270, (Object)logged$create_STAR_$fn__20265$G__20209__20288, (Object)logged$create_STAR_$fn__20265$G__20208__20286, (Object)logged$create_STAR_$fn__20265$G__20198__20266, (Object)logged$create_STAR_$fn__20265$G__20203__20276, (Object)logged$create_STAR_$fn__20265$G__20199__20268, (Object)logged$create_STAR_$fn__20265$G__20206__20282, (Object)logged$create_STAR_$fn__20265$G__20205__20280, (Object)logged$create_STAR_$fn__20265$G__20204__20278, (Object)logged$create_STAR_$fn__20265$G__20201__20272, (Object)logged$create_STAR_$fn__20265$G__20202__20274);
        f__10232__auto__20315 = null;
        Object statearr_20313 = ((IFn)logged$create_STAR_$fn__20265$state_machine__9975__auto____20292).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20313, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20313, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20313;
        statearr_20313 = null;
        Object object2 = state__10233__auto__20316 = object;
        state__10233__auto__20316 = null;
        logged$create_STAR_$fn__20265 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

