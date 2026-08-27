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
import datomic.core2.log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601$fn__20603;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601
extends AFunction {
    Object G__20566;
    Object G__20565;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601(Object object, Object object2) {
        this.G__20566 = object;
        this.G__20565 = object2;
    }

    public Object invoke(Object state_20595) {
        Object ret_value__9977__auto__20626;
        while (true) {
            Object old_frame__9976__auto__20625;
            Object object = old_frame__9976__auto__20625 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20625 = null;
            ret_value__9977__auto__20626 = ((IFn)new log$ensure_tombstone$fn__20596$state_machine__9975__auto____20601$fn__20603(this.G__20566, this.G__20565, state_20595, object)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20626, (Object)const__5)) break;
            Object object2 = state_20595;
            state_20595 = null;
            state_20595 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20626;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20602 = new AtomicReferenceArray(RT.intCast((long)11L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20602, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20602, 1L, const__3);
        Object var1_1 = null;
        return statearr_20602;
    }
}

