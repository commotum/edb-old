/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
 *  com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest
 *  com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;
import datomic.datafy.ObjectToData;

public final class cloudwatch$describe_alarms
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Object const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object o, Object x1) {
        Object object;
        Object object2 = o;
        o = null;
        Object object3 = x1;
        x1 = null;
        DescribeAlarmsResult describeAlarmsResult = ((AmazonCloudWatchClient)object2).describeAlarms((DescribeAlarmsRequest)((IFn)const__1.getRawRoot()).invoke(object3, const__2));
        if (Util.classOf((Object)describeAlarmsResult) != __cached_class__0) {
            if (describeAlarmsResult instanceof ObjectToData) {
                object = ((ObjectToData)describeAlarmsResult).object_to_data();
                return object;
            }
            describeAlarmsResult = describeAlarmsResult;
            __cached_class__0 = Util.classOf((Object)describeAlarmsResult);
        }
        object = const__0.getRawRoot().invoke((Object)describeAlarmsResult);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cloudwatch$describe_alarms.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__2 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest");
    }
}

