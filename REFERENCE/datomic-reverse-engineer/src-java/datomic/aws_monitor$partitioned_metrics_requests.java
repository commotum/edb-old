/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_monitor$partitioned_metrics_requests
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.aws-monitor", (String)"create-request-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"partition-all");
    public static final Object const__4 = 20L;

    public static Object invokeStatic(Object dimensions, Object metrics) {
        Object object = dimensions;
        dimensions = null;
        Object object2 = metrics;
        metrics = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object), ((IFn)const__3.getRawRoot()).invoke(const__4, object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws_monitor$partitioned_metrics_requests.invokeStatic(object3, object4);
    }
}

