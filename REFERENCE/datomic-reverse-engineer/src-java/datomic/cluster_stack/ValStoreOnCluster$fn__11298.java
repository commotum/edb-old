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
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$G__11273__11299;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$G__11274__11301;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$G__11275__11303;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$G__11276__11305;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$G__11277__11307;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$G__11278__11309;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311;

public final class ValStoreOnCluster$fn__11298
extends AFunction {
    Object c__6597__auto__;
    Object ch;
    Object cluster;
    Object captured_bindings__6598__auto__;
    Object v;
    Object _;
    Object k;
    Object opts;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStoreOnCluster$fn__11298(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.c__6597__auto__ = object;
        this.ch = object2;
        this.cluster = object3;
        this.captured_bindings__6598__auto__ = object4;
        this.v = object5;
        this._ = object6;
        this.k = object7;
        this.opts = object8;
    }

    public Object invoke() {
        Object state__6600__auto__11326;
        ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311 f__6599__auto__11325;
        this_.v = null;
        ValStoreOnCluster$fn__11298$G__11273__11299 G__11273 = new ValStoreOnCluster$fn__11298$G__11273__11299(this_.v);
        ValStoreOnCluster$fn__11298$G__11274__11301 G__11274 = new ValStoreOnCluster$fn__11298$G__11274__11301(this_.cluster);
        ValStoreOnCluster$fn__11298$G__11275__11303 G__11275 = new ValStoreOnCluster$fn__11298$G__11275__11303(this_._);
        this_.k = null;
        ValStoreOnCluster$fn__11298$G__11276__11305 G__11276 = new ValStoreOnCluster$fn__11298$G__11276__11305(this_.k);
        this_.ch = null;
        ValStoreOnCluster$fn__11298$G__11277__11307 G__11277 = new ValStoreOnCluster$fn__11298$G__11277__11307(this_.ch);
        this_.opts = null;
        ValStoreOnCluster$fn__11298$G__11278__11309 G__11278 = new ValStoreOnCluster$fn__11298$G__11278__11309(this_.opts);
        ValStoreOnCluster$fn__11298$G__11277__11307 valStoreOnCluster$fn__11298$G__11277__11307 = G__11277;
        G__11277 = null;
        ValStoreOnCluster$fn__11298$G__11273__11299 valStoreOnCluster$fn__11298$G__11273__11299 = G__11273;
        G__11273 = null;
        ValStoreOnCluster$fn__11298$G__11276__11305 valStoreOnCluster$fn__11298$G__11276__11305 = G__11276;
        G__11276 = null;
        ValStoreOnCluster$fn__11298$G__11278__11309 valStoreOnCluster$fn__11298$G__11278__11309 = G__11278;
        G__11278 = null;
        ValStoreOnCluster$fn__11298$G__11274__11301 valStoreOnCluster$fn__11298$G__11274__11301 = G__11274;
        G__11274 = null;
        ValStoreOnCluster$fn__11298$G__11275__11303 valStoreOnCluster$fn__11298$G__11275__11303 = G__11275;
        G__11275 = null;
        ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311 valStoreOnCluster$fn__11298$state_machine__6360__auto____11311 = f__6599__auto__11325 = new ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311((Object)valStoreOnCluster$fn__11298$G__11277__11307, (Object)valStoreOnCluster$fn__11298$G__11273__11299, (Object)valStoreOnCluster$fn__11298$G__11276__11305, (Object)valStoreOnCluster$fn__11298$G__11278__11309, (Object)valStoreOnCluster$fn__11298$G__11274__11301, (Object)valStoreOnCluster$fn__11298$G__11275__11303);
        f__6599__auto__11325 = null;
        Object statearr_11323 = ((IFn)valStoreOnCluster$fn__11298$state_machine__6360__auto____11311).invoke();
        this_.c__6597__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_11323, 5L, this_.c__6597__auto__);
        this_.captured_bindings__6598__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_11323, 3L, this_.captured_bindings__6598__auto__);
        Object object = statearr_11323;
        statearr_11323 = null;
        Object object2 = state__6600__auto__11326 = object;
        state__6600__auto__11326 = null;
        ValStoreOnCluster$fn__11298 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

