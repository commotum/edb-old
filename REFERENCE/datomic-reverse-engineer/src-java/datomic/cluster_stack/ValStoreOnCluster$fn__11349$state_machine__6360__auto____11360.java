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
import datomic.cluster_stack.ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360$fn__11362;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360
extends AFunction {
    Object G__11331;
    Object G__11327;
    Object G__11329;
    Object G__11328;
    Object G__11330;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.G__11331 = object;
        this.G__11327 = object2;
        this.G__11329 = object3;
        this.G__11328 = object4;
        this.G__11330 = object5;
    }

    public Object invoke(Object state_11348) {
        Object ret_value__6362__auto__11371;
        while (true) {
            Object old_frame__6361__auto__11370;
            Object object = old_frame__6361__auto__11370 = Var.getThreadBindingFrame();
            old_frame__6361__auto__11370 = null;
            ret_value__6362__auto__11371 = ((IFn)new ValStoreOnCluster$fn__11349$state_machine__6360__auto____11360$fn__11362(this.G__11331, this.G__11327, this.G__11329, object, this.G__11328, this.G__11330, state_11348)).invoke();
            if (!Util.identical((Object)ret_value__6362__auto__11371, (Object)const__5)) break;
            Object object2 = state_11348;
            state_11348 = null;
            state_11348 = object2;
        }
        Object var3_3 = null;
        return ret_value__6362__auto__11371;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_11361 = new AtomicReferenceArray(RT.intCast((long)11L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_11361, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_11361, 1L, const__3);
        Object var1_1 = null;
        return statearr_11361;
    }
}

