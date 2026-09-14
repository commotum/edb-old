/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class aws_monitor$create_cloudwatch_reporter$fn__23632
extends AFunction {
    Object aws_cloudwatch_dimension_value;
    Object name;
    Object client;
    public static final Var const__0 = RT.var((String)"datomic.aws-monitor", (String)"report-metrics");
    public static final Keyword const__1 = RT.keyword(null, (String)"name");
    public static final Keyword const__2 = RT.keyword(null, (String)"value");

    public aws_monitor$create_cloudwatch_reporter$fn__23632(Object object, Object object2, Object object3) {
        this.aws_cloudwatch_dimension_value = object;
        this.name = object2;
        this.client = object3;
    }

    public Object invoke(Object metrics) {
        Object object = metrics;
        metrics = null;
        aws_monitor$create_cloudwatch_reporter$fn__23632 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.client, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, this_.name, const__2, this_.aws_cloudwatch_dimension_value})), object);
    }
}

