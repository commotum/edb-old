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
package datomic.core2.log.ddb;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.log.ddb.Log$fn__20770$state_machine__9975__auto____20797$fn__20800;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class Log$fn__20770$state_machine__9975__auto____20797
extends AFunction {
    Object G__20652;
    Object G__20663;
    Object G__20657;
    Object G__20659;
    Object G__20662;
    Object G__20653;
    Object G__20661;
    Object G__20658;
    Object G__20651;
    Object G__20655;
    Object G__20660;
    Object G__20654;
    Object G__20656;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public Log$fn__20770$state_machine__9975__auto____20797(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13) {
        this.G__20652 = object;
        this.G__20663 = object2;
        this.G__20657 = object3;
        this.G__20659 = object4;
        this.G__20662 = object5;
        this.G__20653 = object6;
        this.G__20661 = object7;
        this.G__20658 = object8;
        this.G__20651 = object9;
        this.G__20655 = object10;
        this.G__20660 = object11;
        this.G__20654 = object12;
        this.G__20656 = object13;
    }

    public Object invoke(Object state_20769) {
        Object ret_value__9977__auto__20850;
        while (true) {
            Object old_frame__9976__auto__20849;
            Object object = old_frame__9976__auto__20849 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20849 = null;
            ret_value__9977__auto__20850 = ((IFn)new Log$fn__20770$state_machine__9975__auto____20797$fn__20800(this.G__20652, this.G__20663, this.G__20657, this.G__20659, this.G__20662, this.G__20653, this.G__20661, this.G__20658, object, this.G__20651, this.G__20655, state_20769, this.G__20660, this.G__20654, this.G__20656)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20850, (Object)const__5)) break;
            Object object2 = state_20769;
            state_20769 = null;
            state_20769 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20850;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20798 = new AtomicReferenceArray(RT.intCast((long)29L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20798, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20798, 1L, const__3);
        Object var1_1 = null;
        return statearr_20798;
    }
}

