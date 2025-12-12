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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498
extends AFunction {
    Object G__20462;
    Object G__20464;
    Object G__20463;
    Object G__20465;
    Object G__20466;
    public static final Var const__1 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__3 = 1L;
    public static final Keyword const__5 = RT.keyword(null, (String)"recur");

    public aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.G__20462 = object;
        this.G__20464 = object2;
        this.G__20463 = object3;
        this.G__20465 = object4;
        this.G__20466 = object5;
    }

    public Object invoke(Object state_20486) {
        Object ret_value__9977__auto__20514;
        while (true) {
            Object old_frame__9976__auto__20513;
            Object object = old_frame__9976__auto__20513 = Var.getThreadBindingFrame();
            old_frame__9976__auto__20513 = null;
            ret_value__9977__auto__20514 = ((IFn)new aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500(object, state_20486, this.G__20462, this.G__20464, this.G__20463, this.G__20465, this.G__20466)).invoke();
            if (!Util.identical((Object)ret_value__9977__auto__20514, (Object)const__5)) break;
            Object object2 = state_20486;
            state_20486 = null;
            state_20486 = object2;
        }
        Object var3_3 = null;
        return ret_value__9977__auto__20514;
    }

    public Object invoke() {
        AtomicReferenceArray statearr_20499 = new AtomicReferenceArray(RT.intCast((long)11L));
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20499, 0L, (Object)this);
        ((IFn.OLOO)const__1.getRawRoot()).invokePrim(statearr_20499, 1L, const__3);
        Object var1_1 = null;
        return statearr_20499;
    }
}

