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
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500$fn__21502;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500
extends AFunction {
    Object G__21430;
    Object G__21431;
    Object G__21433;
    Object G__21439;
    Object G__21436;
    Object G__21434;
    Object G__21437;
    Object G__21438;
    Object G__21435;
    Object G__21432;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.G__21430 = object;
        this.G__21431 = object2;
        this.G__21433 = object3;
        this.G__21439 = object4;
        this.G__21436 = object5;
        this.G__21434 = object6;
        this.G__21437 = object7;
        this.G__21438 = object8;
        this.G__21435 = object9;
        this.G__21432 = object10;
    }

    public Object invoke(Object state_21478) {
        Object ret_value__9977__auto__21523;
        while (true) {
            Object old_frame__9976__auto__21522;
            Object object = old_frame__9976__auto__21522 = Var.getThreadBindingFrame();
            old_frame__9976__auto__21522 = null;
            ret_value__9977__auto__21523 = ((IFn)new ValStore$fn__21429$fn__21479$state_machine__9975__auto____21500$fn__21502(this.G__21430, this.G__21431, this.G__21433, state_21478, this.G__21439, this.G__21436, this.G__21434, this.G__21437, this.G__21438, this.G__21435, this.G__21432, object)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__21523, (Object)const__5)) break;
            Object object2 = state_21478;
            state_21478 = null;
            state_21478 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__21523;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_21501 = new AtomicReferenceArray(RT.intCast((long)18L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21501, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_21501, 1L, const__3);
        Object var1_1 = null;
        return statearr_21501;
    }
}

