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
import datomic.core2.atom.logged$create_STAR_$fn__20265$state_machine__9975__auto____20292$fn__20294;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class logged$create_STAR_$fn__20265$state_machine__9975__auto____20292
extends AFunction {
    Object G__20210;
    Object G__20207;
    Object G__20200;
    Object G__20209;
    Object G__20208;
    Object G__20198;
    Object G__20203;
    Object G__20199;
    Object G__20206;
    Object G__20205;
    Object G__20204;
    Object G__20201;
    Object G__20202;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public logged$create_STAR_$fn__20265$state_machine__9975__auto____20292(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13) {
        this.G__20210 = object;
        this.G__20207 = object2;
        this.G__20200 = object3;
        this.G__20209 = object4;
        this.G__20208 = object5;
        this.G__20198 = object6;
        this.G__20203 = object7;
        this.G__20199 = object8;
        this.G__20206 = object9;
        this.G__20205 = object10;
        this.G__20204 = object11;
        this.G__20201 = object12;
        this.G__20202 = object13;
    }

    public Object invoke(Object state_20264) {
        Object ret_value__9977__auto__20312;
        while (true) {
            Object old_frame__9976__auto__20311;
            Object object = old_frame__9976__auto__20311 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20311 = null;
            ret_value__9977__auto__20312 = ((IFn)new logged$create_STAR_$fn__20265$state_machine__9975__auto____20292$fn__20294(this.G__20210, object, state_20264, this.G__20207, this.G__20200, this.G__20209, this.G__20208, this.G__20198, this.G__20203, this.G__20199, this.G__20206, this.G__20205, this.G__20204, this.G__20201, this.G__20202)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20312, (Object)const__5)) break;
            Object object2 = state_20264;
            state_20264 = null;
            state_20264 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20312;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20293 = new AtomicReferenceArray(RT.intCast((long)23L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20293, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20293, 1L, const__3);
        Object var1_1 = null;
        return statearr_20293;
    }
}

