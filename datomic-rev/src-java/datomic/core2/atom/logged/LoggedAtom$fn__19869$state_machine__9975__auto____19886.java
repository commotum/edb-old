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
import datomic.core2.atom.logged.LoggedAtom$fn__19869$state_machine__9975__auto____19886$fn__19888;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class LoggedAtom$fn__19869$state_machine__9975__auto____19886
extends AFunction {
    Object G__19785;
    Object G__19786;
    Object G__19788;
    Object G__19789;
    Object G__19783;
    Object G__19787;
    Object G__19782;
    Object G__19784;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public LoggedAtom$fn__19869$state_machine__9975__auto____19886(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.G__19785 = object;
        this.G__19786 = object2;
        this.G__19788 = object3;
        this.G__19789 = object4;
        this.G__19783 = object5;
        this.G__19787 = object6;
        this.G__19782 = object7;
        this.G__19784 = object8;
    }

    public Object invoke(Object state_19868) {
        Object ret_value__9977__auto__19933;
        while (true) {
            Object old_frame__9976__auto__19932;
            Object object = old_frame__9976__auto__19932 = Var.getThreadBindingFrame();
            old_frame__9976__auto__19932 = null;
            ret_value__9977__auto__19933 = ((IFn)new LoggedAtom$fn__19869$state_machine__9975__auto____19886$fn__19888(state_19868, this.G__19785, this.G__19786, this.G__19788, this.G__19789, this.G__19783, object, this.G__19787, this.G__19782, this.G__19784)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__19933, (Object)const__5)) break;
            Object object2 = state_19868;
            state_19868 = null;
            state_19868 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__19933;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_19887 = new AtomicReferenceArray(RT.intCast((long)21L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19887, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_19887, 1L, const__3);
        Object var1_1 = null;
        return statearr_19887;
    }
}

