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
import datomic.core2.async$retry$fn__19535$state_machine__9975__auto____19550$fn__19552;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class async$retry$fn__19535$state_machine__9975__auto____19550
extends AFunction {
    Object G__19482;
    Object G__19480;
    Object G__19485;
    Object G__19481;
    Object G__19483;
    Object G__19484;
    Object G__19486;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public async$retry$fn__19535$state_machine__9975__auto____19550(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.G__19482 = object;
        this.G__19480 = object2;
        this.G__19485 = object3;
        this.G__19481 = object4;
        this.G__19483 = object5;
        this.G__19484 = object6;
        this.G__19486 = object7;
    }

    public Object invoke(Object state_19534) {
        Object ret_value__9977__auto__19575;
        while (true) {
            Object old_frame__9976__auto__19574;
            Object object = old_frame__9976__auto__19574 = Var.getThreadBindingFrame();
            old_frame__9976__auto__19574 = null;
            ret_value__9977__auto__19575 = ((IFn)new async$retry$fn__19535$state_machine__9975__auto____19550$fn__19552(this.G__19482, object, this.G__19480, state_19534, this.G__19485, this.G__19481, this.G__19483, this.G__19484, this.G__19486)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__19575, (Object)const__5)) break;
            Object object2 = state_19534;
            state_19534 = null;
            state_19534 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__19575;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_19551 = new AtomicReferenceArray(RT.intCast((long)17L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19551, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19551, 1L, const__3);
        Object var1_1 = null;
        return statearr_19551;
    }
}

