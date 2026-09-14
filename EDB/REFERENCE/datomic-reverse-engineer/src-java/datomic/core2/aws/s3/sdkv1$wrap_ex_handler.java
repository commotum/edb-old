/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.aws.s3.sdkv1$wrap_ex_handler$fn__20533;

public final class sdkv1$wrap_ex_handler
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"wrap-ex-handler");

    public static Object invokeStatic(Object f, Object context) {
        Object object = f;
        f = null;
        Object object2 = context;
        context = null;
        return new sdkv1$wrap_ex_handler$fn__20533(object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return sdkv1$wrap_ex_handler.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$wrap_ex_handler.invokeStatic(object2);
    }
}

