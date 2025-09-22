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
import datomic.core2.atom.logged$create$fn__20351$G__20322__20352;
import datomic.core2.atom.logged$create$fn__20351$G__20323__20354;
import datomic.core2.atom.logged$create$fn__20351$G__20324__20356;
import datomic.core2.atom.logged$create$fn__20351$G__20325__20358;
import datomic.core2.atom.logged$create$fn__20351$G__20326__20360;
import datomic.core2.atom.logged$create$fn__20351$G__20327__20362;
import datomic.core2.atom.logged$create$fn__20351$G__20328__20364;
import datomic.core2.atom.logged$create$fn__20351$state_machine__9975__auto____20366;

public final class logged$create$fn__20351
extends AFunction {
    Object map__20321;
    Object captured_bindings__10231__auto__;
    Object log;
    Object c__10230__auto__;
    Object args;
    Object header;
    Object value;
    Object serialize;
    Object p__20320;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public logged$create$fn__20351(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.map__20321 = object;
        this.captured_bindings__10231__auto__ = object2;
        this.log = object3;
        this.c__10230__auto__ = object4;
        this.args = object5;
        this.header = object6;
        this.value = object7;
        this.serialize = object8;
        this.p__20320 = object9;
    }

    public Object invoke() {
        Object state__10233__auto__20382;
        logged$create$fn__20351$state_machine__9975__auto____20366 f__10232__auto__20381;
        logged$create$fn__20351$G__20328__20364 G__20328;
        this_.p__20320 = null;
        logged$create$fn__20351$G__20322__20352 G__20322 = new logged$create$fn__20351$G__20322__20352(this_.p__20320);
        this_.map__20321 = null;
        logged$create$fn__20351$G__20323__20354 G__20323 = new logged$create$fn__20351$G__20323__20354(this_.map__20321);
        this_.args = null;
        logged$create$fn__20351$G__20324__20356 G__20324 = new logged$create$fn__20351$G__20324__20356(this_.args);
        this_.log = null;
        logged$create$fn__20351$G__20325__20358 G__20325 = new logged$create$fn__20351$G__20325__20358(this_.log);
        this_.header = null;
        logged$create$fn__20351$G__20326__20360 G__20326 = new logged$create$fn__20351$G__20326__20360(this_.header);
        this_.value = null;
        logged$create$fn__20351$G__20327__20362 G__20327 = new logged$create$fn__20351$G__20327__20362(this_.value);
        this_.serialize = null;
        logged$create$fn__20351$G__20328__20364 logged$create$fn__20351$G__20328__20364 = G__20328 = new logged$create$fn__20351$G__20328__20364(this_.serialize);
        G__20328 = null;
        logged$create$fn__20351$G__20326__20360 logged$create$fn__20351$G__20326__20360 = G__20326;
        G__20326 = null;
        logged$create$fn__20351$G__20327__20362 logged$create$fn__20351$G__20327__20362 = G__20327;
        G__20327 = null;
        logged$create$fn__20351$G__20325__20358 logged$create$fn__20351$G__20325__20358 = G__20325;
        G__20325 = null;
        logged$create$fn__20351$G__20322__20352 logged$create$fn__20351$G__20322__20352 = G__20322;
        G__20322 = null;
        logged$create$fn__20351$G__20324__20356 logged$create$fn__20351$G__20324__20356 = G__20324;
        G__20324 = null;
        logged$create$fn__20351$G__20323__20354 logged$create$fn__20351$G__20323__20354 = G__20323;
        G__20323 = null;
        logged$create$fn__20351$state_machine__9975__auto____20366 logged$create$fn__20351$state_machine__9975__auto____20366 = f__10232__auto__20381 = new logged$create$fn__20351$state_machine__9975__auto____20366((Object)logged$create$fn__20351$G__20328__20364, (Object)logged$create$fn__20351$G__20326__20360, (Object)logged$create$fn__20351$G__20327__20362, (Object)logged$create$fn__20351$G__20325__20358, (Object)logged$create$fn__20351$G__20322__20352, (Object)logged$create$fn__20351$G__20324__20356, (Object)logged$create$fn__20351$G__20323__20354);
        f__10232__auto__20381 = null;
        Object statearr_20379 = ((IFn)logged$create$fn__20351$state_machine__9975__auto____20366).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20379, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20379, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20379;
        statearr_20379 = null;
        Object object2 = state__10233__auto__20382 = object;
        state__10233__auto__20382 = null;
        logged$create$fn__20351 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

