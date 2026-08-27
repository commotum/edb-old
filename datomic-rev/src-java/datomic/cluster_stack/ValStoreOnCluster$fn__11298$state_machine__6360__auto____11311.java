/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cluster_stack;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster_stack.ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311$fn__11313;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311
extends AFunction {
    Object G__11277;
    Object G__11273;
    Object G__11276;
    Object G__11278;
    Object G__11274;
    Object G__11275;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.G__11277 = object;
        this.G__11273 = object2;
        this.G__11276 = object3;
        this.G__11278 = object4;
        this.G__11274 = object5;
        this.G__11275 = object6;
    }

    public Object invoke(Object state_11297) {
        Object ret_value__6362__auto__11322;
        while (true) {
            Object old_frame__6361__auto__11321;
            Object object = old_frame__6361__auto__11321 = Var.getThreadBindingFrame();
            old_frame__6361__auto__11321 = null;
            ret_value__6362__auto__11322 = ((IFn)new ValStoreOnCluster$fn__11298$state_machine__6360__auto____11311$fn__11313(this.G__11277, this.G__11273, this.G__11276, this.G__11278, this.G__11274, this.G__11275, state_11297, object)).invoke();
            if (!Util.identical((Object)ret_value__6362__auto__11322, (Object)const__5)) break;
            Object object2 = state_11297;
            state_11297 = null;
            state_11297 = object2;
        }
        Object var3_3 = null;
        return ret_value__6362__auto__11322;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_11312 = new AtomicReferenceArray(RT.intCast((long)12L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_11312, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_11312, 1L, const__3);
        Object var1_1 = null;
        return statearr_11312;
    }
}

