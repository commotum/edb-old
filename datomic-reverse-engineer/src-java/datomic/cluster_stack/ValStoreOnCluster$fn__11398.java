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
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$G__11376__11399;
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$G__11377__11401;
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$G__11378__11403;
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$G__11379__11405;
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$G__11380__11407;
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409;

public final class ValStoreOnCluster$fn__11398
extends AFunction {
    Object opts;
    Object k;
    Object _;
    Object cluster;
    Object captured_bindings__6598__auto__;
    Object c__6597__auto__;
    Object ch;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStoreOnCluster$fn__11398(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.opts = object;
        this.k = object2;
        this._ = object3;
        this.cluster = object4;
        this.captured_bindings__6598__auto__ = object5;
        this.c__6597__auto__ = object6;
        this.ch = object7;
    }

    public Object invoke() {
        Object state__6600__auto__11424;
        ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409 f__6599__auto__11423;
        ValStoreOnCluster$fn__11398$G__11376__11399 G__11376 = new ValStoreOnCluster$fn__11398$G__11376__11399(this_.cluster);
        ValStoreOnCluster$fn__11398$G__11377__11401 G__11377 = new ValStoreOnCluster$fn__11398$G__11377__11401(this_._);
        this_.k = null;
        ValStoreOnCluster$fn__11398$G__11378__11403 G__11378 = new ValStoreOnCluster$fn__11398$G__11378__11403(this_.k);
        this_.ch = null;
        ValStoreOnCluster$fn__11398$G__11379__11405 G__11379 = new ValStoreOnCluster$fn__11398$G__11379__11405(this_.ch);
        this_.opts = null;
        ValStoreOnCluster$fn__11398$G__11380__11407 G__11380 = new ValStoreOnCluster$fn__11398$G__11380__11407(this_.opts);
        ValStoreOnCluster$fn__11398$G__11379__11405 valStoreOnCluster$fn__11398$G__11379__11405 = G__11379;
        G__11379 = null;
        ValStoreOnCluster$fn__11398$G__11378__11403 valStoreOnCluster$fn__11398$G__11378__11403 = G__11378;
        G__11378 = null;
        ValStoreOnCluster$fn__11398$G__11376__11399 valStoreOnCluster$fn__11398$G__11376__11399 = G__11376;
        G__11376 = null;
        ValStoreOnCluster$fn__11398$G__11377__11401 valStoreOnCluster$fn__11398$G__11377__11401 = G__11377;
        G__11377 = null;
        ValStoreOnCluster$fn__11398$G__11380__11407 valStoreOnCluster$fn__11398$G__11380__11407 = G__11380;
        G__11380 = null;
        ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409 valStoreOnCluster$fn__11398$state_machine__6360__auto____11409 = f__6599__auto__11423 = new ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409((Object)valStoreOnCluster$fn__11398$G__11379__11405, (Object)valStoreOnCluster$fn__11398$G__11378__11403, (Object)valStoreOnCluster$fn__11398$G__11376__11399, (Object)valStoreOnCluster$fn__11398$G__11377__11401, (Object)valStoreOnCluster$fn__11398$G__11380__11407);
        f__6599__auto__11423 = null;
        Object statearr_11421 = ((IFn)valStoreOnCluster$fn__11398$state_machine__6360__auto____11409).invoke();
        this_.c__6597__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_11421, 5L, this_.c__6597__auto__);
        this_.captured_bindings__6598__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_11421, 3L, this_.captured_bindings__6598__auto__);
        Object object = statearr_11421;
        statearr_11421 = null;
        Object object2 = state__6600__auto__11424 = object;
        state__6600__auto__11424 = null;
        ValStoreOnCluster$fn__11398 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

