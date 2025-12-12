/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.cloudwatch.model.ListMetricsRequest
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;

public final class cloudwatch$list_metrics$fn__22812
extends AFunction {
    Object client;
    Object metrics;
    Object token;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__1 = RT.var((String)"datomic.cloudwatch", (String)"list-metrics");

    public cloudwatch$list_metrics$fn__22812(Object object, Object object2, Object object3) {
        this.client = object;
        this.metrics = object2;
        this.token = object3;
    }

    public Object invoke() {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        ListMetricsRequest G__22813 = new ListMetricsRequest();
        G__22813.setNextToken((String)this_.token);
        Object var1_1 = null;
        cloudwatch$list_metrics$fn__22812 this_ = null;
        return iFn.invoke(this_.metrics, iFn2.invoke(this_.client, (Object)G__22813));
    }
}

