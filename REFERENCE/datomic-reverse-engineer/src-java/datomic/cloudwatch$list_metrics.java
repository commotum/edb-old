/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
 *  com.amazonaws.services.cloudwatch.model.ListMetricsRequest
 *  com.amazonaws.services.cloudwatch.model.ListMetricsResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import datomic.cloudwatch$list_metrics$fn__22812;
import java.util.List;

public final class cloudwatch$list_metrics
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cloudwatch", (String)"list-metrics");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Object const__2 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.ListMetricsRequest");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data");

    public static Object invokeStatic(Object client2, Object request) {
        List list;
        String temp__5455__auto__22816;
        ListMetricsResult listMetricsResult;
        Object object = request;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = request;
            request = null;
            listMetricsResult = ((AmazonCloudWatchClient)client2).listMetrics((ListMetricsRequest)((IFn)const__1.getRawRoot()).invoke(object2, const__2));
        } else {
            listMetricsResult = ((AmazonCloudWatchClient)client2).listMetrics();
        }
        ListMetricsResult result2 = listMetricsResult;
        List metrics = result2.getMetrics();
        IFn iFn = (IFn)const__3.getRawRoot();
        Object object3 = const__4.getRawRoot();
        ListMetricsResult listMetricsResult2 = result2;
        result2 = null;
        String string = temp__5455__auto__22816 = listMetricsResult2.getNextToken();
        if (string != null && string != Boolean.FALSE) {
            String string2 = temp__5455__auto__22816;
            temp__5455__auto__22816 = null;
            String token = string2;
            client2 = null;
            metrics = null;
            token = null;
            list = new LazySeq((IFn)new cloudwatch$list_metrics$fn__22812(client2, metrics, token));
        } else {
            list = metrics;
            metrics = null;
        }
        return iFn.invoke(object3, (Object)list);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cloudwatch$list_metrics.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object client2) {
        Object object = client2;
        client2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cloudwatch$list_metrics.invokeStatic(object2);
    }
}

