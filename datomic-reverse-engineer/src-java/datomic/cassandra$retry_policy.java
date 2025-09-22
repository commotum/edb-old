/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datastax.driver.core.policies.DefaultRetryPolicy
 */
package datomic;

import clojure.lang.AFunction;
import com.datastax.driver.core.policies.DefaultRetryPolicy;

public final class cassandra$retry_policy
extends AFunction {
    public static Object invokeStatic() {
        return DefaultRetryPolicy.INSTANCE;
    }

    public Object invoke() {
        return cassandra$retry_policy.invokeStatic();
    }
}

