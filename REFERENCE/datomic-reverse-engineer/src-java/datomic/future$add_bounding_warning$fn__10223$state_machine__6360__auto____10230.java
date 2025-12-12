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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230$fn__10232;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230
extends AFunction {
    Object G__10182;
    Object G__10181;
    Object G__10183;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230(Object object, Object object2, Object object3) {
        this.G__10182 = object;
        this.G__10181 = object2;
        this.G__10183 = object3;
    }

    public Object invoke(Object state_10222) {
        Object ret_value__6362__auto__10253;
        while (true) {
            Object old_frame__6361__auto__10252;
            Object object = old_frame__6361__auto__10252 = Var.getThreadBindingFrame();
            old_frame__6361__auto__10252 = null;
            ret_value__6362__auto__10253 = ((IFn)new future$add_bounding_warning$fn__10223$state_machine__6360__auto____10230$fn__10232(this.G__10182, this.G__10181, this.G__10183, object, state_10222)).invoke();
            if (!Util.identical((Object)ret_value__6362__auto__10253, (Object)const__5)) break;
            Object object2 = state_10222;
            state_10222 = null;
            state_10222 = object2;
        }
        Object var3_3 = null;
        return ret_value__6362__auto__10253;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_10231 = new AtomicReferenceArray(RT.intCast((long)12L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_10231, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_10231, 1L, const__3);
        Object var1_1 = null;
        return statearr_10231;
    }
}

