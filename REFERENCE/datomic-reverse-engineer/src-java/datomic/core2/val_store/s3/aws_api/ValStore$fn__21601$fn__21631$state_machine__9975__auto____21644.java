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
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644$fn__21646;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644
extends AFunction {
    Object G__21602;
    Object G__21604;
    Object G__21607;
    Object G__21606;
    Object G__21603;
    Object G__21605;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.G__21602 = object;
        this.G__21604 = object2;
        this.G__21607 = object3;
        this.G__21606 = object4;
        this.G__21603 = object5;
        this.G__21605 = object6;
    }

    public Object invoke(Object state_21630) {
        Object ret_value__9977__auto__21657;
        while (true) {
            Object old_frame__9976__auto__21656;
            Object object = old_frame__9976__auto__21656 = Var.getThreadBindingFrame();
            old_frame__9976__auto__21656 = null;
            ret_value__9977__auto__21657 = ((IFn)new ValStore$fn__21601$fn__21631$state_machine__9975__auto____21644$fn__21646(this.G__21602, state_21630, object, this.G__21604, this.G__21607, this.G__21606, this.G__21603, this.G__21605)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__21657, (Object)const__5)) break;
            Object object2 = state_21630;
            state_21630 = null;
            state_21630 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__21657;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_21645 = new AtomicReferenceArray(RT.intCast((long)12L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21645, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21645, 1L, const__3);
        Object var1_1 = null;
        return statearr_21645;
    }
}

