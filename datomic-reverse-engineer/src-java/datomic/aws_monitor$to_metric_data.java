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
import datomic.aws_monitor$to_metric_data$fn__23612;

public final class aws_monitor$to_metric_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");

    public static Object invokeStatic(Object statistics) {
        Object object = statistics;
        statistics = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new aws_monitor$to_metric_data$fn__23612(), object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_monitor$to_metric_data.invokeStatic(object2);
    }
}

