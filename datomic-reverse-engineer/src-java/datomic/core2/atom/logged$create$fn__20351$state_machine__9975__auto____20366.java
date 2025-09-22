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
import datomic.core2.atom.logged$create$fn__20351$state_machine__9975__auto____20366$fn__20368;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class logged$create$fn__20351$state_machine__9975__auto____20366
extends AFunction {
    Object G__20328;
    Object G__20326;
    Object G__20327;
    Object G__20325;
    Object G__20322;
    Object G__20324;
    Object G__20323;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public logged$create$fn__20351$state_machine__9975__auto____20366(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.G__20328 = object;
        this.G__20326 = object2;
        this.G__20327 = object3;
        this.G__20325 = object4;
        this.G__20322 = object5;
        this.G__20324 = object6;
        this.G__20323 = object7;
    }

    public Object invoke(Object state_20350) {
        Object ret_value__9977__auto__20378;
        while (true) {
            Object old_frame__9976__auto__20377;
            Object object = old_frame__9976__auto__20377 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20377 = null;
            ret_value__9977__auto__20378 = ((IFn)new logged$create$fn__20351$state_machine__9975__auto____20366$fn__20368(this.G__20328, this.G__20326, this.G__20327, this.G__20325, this.G__20322, this.G__20324, this.G__20323, state_20350, object)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20378, (Object)const__5)) break;
            Object object2 = state_20350;
            state_20350 = null;
            state_20350 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20378;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20367 = new AtomicReferenceArray(RT.intCast((long)13L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20367, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20367, 1L, const__3);
        Object var1_1 = null;
        return statearr_20367;
    }
}

