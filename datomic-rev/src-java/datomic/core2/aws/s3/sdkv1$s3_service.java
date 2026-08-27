/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.retry.PredefinedRetryPolicies
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.retry.PredefinedRetryPolicies;

public final class sdkv1$s3_service
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"client");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__2 = RT.keyword(null, (String)"retryPolicy");

    public static Object invokeStatic(Object args) {
        Object object = args;
        args = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2, (Object)PredefinedRetryPolicies.NO_RETRY_POLICY));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sdkv1$s3_service.invokeStatic(object2);
    }
}

