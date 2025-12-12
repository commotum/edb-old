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
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.atom.logged$append_value$fn__19744$state_machine__9975__auto____19753$fn__19755;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class logged$append_value$fn__19744$state_machine__9975__auto____19753
extends AFunction {
    Object G__19717;
    Object G__19719;
    Object G__19718;
    Object G__19716;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public logged$append_value$fn__19744$state_machine__9975__auto____19753(Object object, Object object2, Object object3, Object object4) {
        this.G__19717 = object;
        this.G__19719 = object2;
        this.G__19718 = object3;
        this.G__19716 = object4;
    }

    public Object invoke(Object state_19743) {
        Object ret_value__9977__auto__19770;
        while (true) {
            Object old_frame__9976__auto__19769;
            Object object = old_frame__9976__auto__19769 = Var.getThreadBindingFrame();
            old_frame__9976__auto__19769 = null;
            ret_value__9977__auto__19770 = ((IFn)new logged$append_value$fn__19744$state_machine__9975__auto____19753$fn__19755(state_19743, object, this.G__19717, this.G__19719, this.G__19718, this.G__19716)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__19770, (Object)const__5)) break;
            Object object2 = state_19743;
            state_19743 = null;
            state_19743 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__19770;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_19754 = new AtomicReferenceArray(RT.intCast((long)12L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19754, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19754, 1L, const__3);
        Object var1_1 = null;
        return statearr_19754;
    }
}

