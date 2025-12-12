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
import datomic.core2.atom.logged.LoggedAtom$fn__20023$state_machine__9975__auto____20044$fn__20046;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class LoggedAtom$fn__20023$state_machine__9975__auto____20044
extends AFunction {
    Object G__19938;
    Object G__19940;
    Object G__19944;
    Object G__19943;
    Object G__19945;
    Object G__19939;
    Object G__19947;
    Object G__19941;
    Object G__19942;
    Object G__19946;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public LoggedAtom$fn__20023$state_machine__9975__auto____20044(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.G__19938 = object;
        this.G__19940 = object2;
        this.G__19944 = object3;
        this.G__19943 = object4;
        this.G__19945 = object5;
        this.G__19939 = object6;
        this.G__19947 = object7;
        this.G__19941 = object8;
        this.G__19942 = object9;
        this.G__19946 = object10;
    }

    public Object invoke(Object state_20022) {
        Object ret_value__9977__auto__20085;
        while (true) {
            Object old_frame__9976__auto__20084;
            Object object = old_frame__9976__auto__20084 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20084 = null;
            ret_value__9977__auto__20085 = ((IFn)new LoggedAtom$fn__20023$state_machine__9975__auto____20044$fn__20046(this.G__19938, this.G__19940, this.G__19944, this.G__19943, this.G__19945, this.G__19939, this.G__19947, this.G__19941, this.G__19942, object, state_20022, this.G__19946)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20085, (Object)const__5)) break;
            Object object2 = state_20022;
            state_20022 = null;
            state_20022 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20085;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20045 = new AtomicReferenceArray(RT.intCast((long)22L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20045, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20045, 1L, const__3);
        Object var1_1 = null;
        return statearr_20045;
    }
}

