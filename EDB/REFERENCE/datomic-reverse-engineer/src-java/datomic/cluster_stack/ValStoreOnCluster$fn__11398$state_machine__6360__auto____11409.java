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
import datomic.cluster_stack.ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409$fn__11411;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409
extends AFunction {
    Object G__11379;
    Object G__11378;
    Object G__11376;
    Object G__11377;
    Object G__11380;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.G__11379 = object;
        this.G__11378 = object2;
        this.G__11376 = object3;
        this.G__11377 = object4;
        this.G__11380 = object5;
    }

    public Object invoke(Object state_11397) {
        Object ret_value__6362__auto__11420;
        while (true) {
            Object old_frame__6361__auto__11419;
            Object object = old_frame__6361__auto__11419 = Var.getThreadBindingFrame();
            old_frame__6361__auto__11419 = null;
            ret_value__6362__auto__11420 = ((IFn)new ValStoreOnCluster$fn__11398$state_machine__6360__auto____11409$fn__11411(object, this.G__11379, this.G__11378, this.G__11376, state_11397, this.G__11377, this.G__11380)).invoke();
            if (!Util.identical((Object)ret_value__6362__auto__11420, (Object)const__5)) break;
            Object object2 = state_11397;
            state_11397 = null;
            state_11397 = object2;
        }
        Object var3_3 = null;
        return ret_value__6362__auto__11420;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_11410 = new AtomicReferenceArray(RT.intCast((long)11L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_11410, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_11410, 1L, const__3);
        Object var1_1 = null;
        return statearr_11410;
    }
}

