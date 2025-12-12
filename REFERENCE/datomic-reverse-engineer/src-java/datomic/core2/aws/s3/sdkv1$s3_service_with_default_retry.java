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

public final class sdkv1$s3_service_with_default_retry
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"client");

    public static Object invokeStatic(Object args) {
        Object object = args;
        args = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$s3_service_with_default_retry.invokeStatic(object2);
    }
}

