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
package datomic.garbage;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.garbage.pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class pod$schedule_gc$fn__22693$state_machine__6360__auto____22698
extends AFunction {
    Object G__22601;
    Object G__22600;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public pod$schedule_gc$fn__22693$state_machine__6360__auto____22698(Object object, Object object2) {
        this.G__22601 = object;
        this.G__22600 = object2;
    }

    public Object invoke(Object state_22692) {
        Object ret_value__6362__auto__22734;
        while (true) {
            Object old_frame__6361__auto__22733;
            Object object = old_frame__6361__auto__22733 = Var.getThreadBindingFrame();
            old_frame__6361__auto__22733 = null;
            ret_value__6362__auto__22734 = ((IFn)new pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700(state_22692, this.G__22601, object, this.G__22600)).invoke();
            if (!Util.identical((Object)ret_value__6362__auto__22734, (Object)const__5)) break;
            Object object2 = state_22692;
            state_22692 = null;
            state_22692 = object2;
        }
        Object var3_3 = null;
        return ret_value__6362__auto__22734;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_22699 = new AtomicReferenceArray(RT.intCast((long)30L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_22699, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_22699, 1L, const__3);
        Object var1_1 = null;
        return statearr_22699;
    }
}

