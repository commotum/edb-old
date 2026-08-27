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
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579
extends AFunction {
    Object G__21534;
    Object G__21535;
    Object G__21532;
    Object G__21533;
    Object G__21537;
    Object G__21536;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.G__21534 = object;
        this.G__21535 = object2;
        this.G__21532 = object3;
        this.G__21533 = object4;
        this.G__21537 = object5;
        this.G__21536 = object6;
    }

    public Object invoke(Object state_21565) {
        Object ret_value__9977__auto__21593;
        while (true) {
            Object old_frame__9976__auto__21592;
            Object object = old_frame__9976__auto__21592 = Var.getThreadBindingFrame();
            old_frame__9976__auto__21592 = null;
            ret_value__9977__auto__21593 = ((IFn)new ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581(this.G__21534, this.G__21535, this.G__21532, state_21565, object, this.G__21533, this.G__21537, this.G__21536)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__21593, (Object)const__5)) break;
            Object object2 = state_21565;
            state_21565 = null;
            state_21565 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__21593;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_21580 = new AtomicReferenceArray(RT.intCast((long)13L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21580, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21580, 1L, const__3);
        Object var1_1 = null;
        return statearr_21580;
    }
}

