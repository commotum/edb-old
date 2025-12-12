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
package datomic.core2.val_store.double_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.double_store.ValStore$fn__21186$state_machine__9975__auto____21201$fn__21203;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStore$fn__21186$state_machine__9975__auto____21201
extends AFunction {
    Object G__21087;
    Object G__21090;
    Object G__21091;
    Object G__21088;
    Object G__21089;
    Object G__21092;
    Object G__21093;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStore$fn__21186$state_machine__9975__auto____21201(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.G__21087 = object;
        this.G__21090 = object2;
        this.G__21091 = object3;
        this.G__21088 = object4;
        this.G__21089 = object5;
        this.G__21092 = object6;
        this.G__21093 = object7;
    }

    public Object invoke(Object state_21185) {
        Object ret_value__9977__auto__21240;
        while (true) {
            Object old_frame__9976__auto__21239;
            Object object = old_frame__9976__auto__21239 = Var.getThreadBindingFrame();
            old_frame__9976__auto__21239 = null;
            ret_value__9977__auto__21240 = ((IFn)new ValStore$fn__21186$state_machine__9975__auto____21201$fn__21203(this.G__21087, this.G__21090, this.G__21091, state_21185, this.G__21088, object, this.G__21089, this.G__21092, this.G__21093)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__21240, (Object)const__5)) break;
            Object object2 = state_21185;
            state_21185 = null;
            state_21185 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__21240;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_21202 = new AtomicReferenceArray(RT.intCast((long)30L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21202, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21202, 1L, const__3);
        Object var1_1 = null;
        return statearr_21202;
    }
}

