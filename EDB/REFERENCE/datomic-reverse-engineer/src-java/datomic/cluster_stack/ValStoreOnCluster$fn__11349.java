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
package datomic.cluster_stack;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$G__11327__11350;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$G__11328__11352;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$G__11329__11354;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$G__11330__11356;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$G__11331__11358;
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360;

public final class ValStoreOnCluster$fn__11349
extends AFunction {
    Object captured_bindings__6598__auto__;
    Object c__6597__auto__;
    Object opts;
    Object k;
    Object cluster;
    Object _;
    Object ch;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStoreOnCluster$fn__11349(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.captured_bindings__6598__auto__ = object;
        this.c__6597__auto__ = object2;
        this.opts = object3;
        this.k = object4;
        this.cluster = object5;
        this._ = object6;
        this.ch = object7;
    }

    public Object invoke() {
        Object state__6600__auto__11375;
        ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360 f__6599__auto__11374;
        ValStoreOnCluster$fn__11349$G__11331__11358 G__11331;
        ValStoreOnCluster$fn__11349$G__11327__11350 G__11327 = new ValStoreOnCluster$fn__11349$G__11327__11350(this_.cluster);
        ValStoreOnCluster$fn__11349$G__11328__11352 G__11328 = new ValStoreOnCluster$fn__11349$G__11328__11352(this_._);
        this_.k = null;
        ValStoreOnCluster$fn__11349$G__11329__11354 G__11329 = new ValStoreOnCluster$fn__11349$G__11329__11354(this_.k);
        this_.ch = null;
        ValStoreOnCluster$fn__11349$G__11330__11356 G__11330 = new ValStoreOnCluster$fn__11349$G__11330__11356(this_.ch);
        this_.opts = null;
        ValStoreOnCluster$fn__11349$G__11331__11358 valStoreOnCluster$fn__11349$G__11331__11358 = G__11331 = new ValStoreOnCluster$fn__11349$G__11331__11358(this_.opts);
        G__11331 = null;
        ValStoreOnCluster$fn__11349$G__11327__11350 valStoreOnCluster$fn__11349$G__11327__11350 = G__11327;
        G__11327 = null;
        ValStoreOnCluster$fn__11349$G__11329__11354 valStoreOnCluster$fn__11349$G__11329__11354 = G__11329;
        G__11329 = null;
        ValStoreOnCluster$fn__11349$G__11328__11352 valStoreOnCluster$fn__11349$G__11328__11352 = G__11328;
        G__11328 = null;
        ValStoreOnCluster$fn__11349$G__11330__11356 valStoreOnCluster$fn__11349$G__11330__11356 = G__11330;
        G__11330 = null;
        ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360 valStoreOnCluster$fn__11349$state_machine__6360__auto____11360 = f__6599__auto__11374 = new ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360((Object)valStoreOnCluster$fn__11349$G__11331__11358, (Object)valStoreOnCluster$fn__11349$G__11327__11350, (Object)valStoreOnCluster$fn__11349$G__11329__11354, (Object)valStoreOnCluster$fn__11349$G__11328__11352, (Object)valStoreOnCluster$fn__11349$G__11330__11356);
        f__6599__auto__11374 = null;
        Object statearr_11372 = ((IFn)valStoreOnCluster$fn__11349$state_machine__6360__auto____11360).invoke();
        this_.c__6597__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_11372, 5L, this_.c__6597__auto__);
        this_.captured_bindings__6598__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_11372, 3L, this_.captured_bindings__6598__auto__);
        Object object = statearr_11372;
        statearr_11372 = null;
        Object object2 = state__6600__auto__11375 = object;
        state__6600__auto__11375 = null;
        ValStoreOnCluster$fn__11349 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

