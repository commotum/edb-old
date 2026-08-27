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
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387
extends AFunction {
    Object G__21355;
    Object G__21353;
    Object G__21354;
    Object G__21352;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387(Object object, Object object2, Object object3, Object object4) {
        this.G__21355 = object;
        this.G__21353 = object2;
        this.G__21354 = object3;
        this.G__21352 = object4;
    }

    public Object invoke(Object state_21377) {
        Object ret_value__9977__auto__21400;
        while (true) {
            Object old_frame__9976__auto__21399;
            Object object = old_frame__9976__auto__21399 = Var.getThreadBindingFrame();
            old_frame__9976__auto__21399 = null;
            ret_value__9977__auto__21400 = ((IFn)new s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389(object, this.G__21355, this.G__21353, state_21377, this.G__21354, this.G__21352)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__21400, (Object)const__5)) break;
            Object object2 = state_21377;
            state_21377 = null;
            state_21377 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__21400;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_21388 = new AtomicReferenceArray(RT.intCast((long)11L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21388, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21388, 1L, const__3);
        Object var1_1 = null;
        return statearr_21388;
    }
}

