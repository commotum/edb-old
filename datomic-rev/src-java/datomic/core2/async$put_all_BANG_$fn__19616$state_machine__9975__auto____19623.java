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
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623$fn__19625;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623
extends AFunction {
    Object G__19584;
    Object G__19585;
    Object G__19583;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623(Object object, Object object2, Object object3) {
        this.G__19584 = object;
        this.G__19585 = object2;
        this.G__19583 = object3;
    }

    public Object invoke(Object state_19615) {
        Object ret_value__9977__auto__19645;
        while (true) {
            Object old_frame__9976__auto__19644;
            Object object = old_frame__9976__auto__19644 = Var.getThreadBindingFrame();
            old_frame__9976__auto__19644 = null;
            ret_value__9977__auto__19645 = ((IFn)new async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623$fn__19625(this.G__19584, object, state_19615, this.G__19585, this.G__19583)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__19645, (Object)const__5)) break;
            Object object2 = state_19615;
            state_19615 = null;
            state_19615 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__19645;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_19624 = new AtomicReferenceArray(RT.intCast((long)10L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19624, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19624, 1L, const__3);
        Object var1_1 = null;
        return statearr_19624;
    }
}

