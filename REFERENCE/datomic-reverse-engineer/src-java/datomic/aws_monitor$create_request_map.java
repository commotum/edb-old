/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.aws_monitor$create_request_map$fn__23619;

public final class aws_monitor$create_request_map
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"namespace");
    public static final Keyword const__1 = RT.keyword(null, (String)"metricData");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__3 = RT.var((String)"datomic.aws-monitor", (String)"to-metric-data");

    public static Object invokeStatic(Object dimensions, Object metrics) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        objectArray[1] = "Datomic";
        objectArray[2] = const__1;
        Object object = dimensions;
        dimensions = null;
        Object object2 = metrics;
        metrics = null;
        objectArray[3] = ((IFn)const__2.getRawRoot()).invoke((Object)new aws_monitor$create_request_map$fn__23619(object), ((IFn)const__3.getRawRoot()).invoke(object2));
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws_monitor$create_request_map.invokeStatic(object3, object4);
    }
}

