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
package datomic.core2.atom.logged;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$state_machine__9975__auto____20154$fn__20156;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class LoggedAtom$fn__20135$state_machine__9975__auto____20154
extends AFunction {
    Object G__20092;
    Object G__20097;
    Object G__20091;
    Object G__20096;
    Object G__20098;
    Object G__20093;
    Object G__20094;
    Object G__20090;
    Object G__20095;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public LoggedAtom$fn__20135$state_machine__9975__auto____20154(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.G__20092 = object;
        this.G__20097 = object2;
        this.G__20091 = object3;
        this.G__20096 = object4;
        this.G__20098 = object5;
        this.G__20093 = object6;
        this.G__20094 = object7;
        this.G__20090 = object8;
        this.G__20095 = object9;
    }

    public Object invoke(Object state_20134) {
        Object ret_value__9977__auto__20166;
        while (true) {
            Object old_frame__9976__auto__20165;
            Object object = old_frame__9976__auto__20165 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20165 = null;
            ret_value__9977__auto__20166 = ((IFn)new LoggedAtom$fn__20135$state_machine__9975__auto____20154$fn__20156(object, this.G__20092, this.G__20097, state_20134, this.G__20091, this.G__20096, this.G__20098, this.G__20093, this.G__20094, this.G__20090, this.G__20095)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20166, (Object)const__5)) break;
            Object object2 = state_20134;
            state_20134 = null;
            state_20134 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20166;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20155 = new AtomicReferenceArray(RT.intCast((long)16L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20155, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20155, 1L, const__3);
        Object var1_1 = null;
        return statearr_20155;
    }
}

