/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class config$ddb_client_args
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"property-map");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"clientExecutionTimeout"), "datomic.ddbClientExecutionTimeout", RT.keyword(null, (String)"connectionTimeout"), "datomic.ddbConnectionTimeout", RT.keyword(null, (String)"requestTimeout"), "datomic.ddbRequestTimeout", RT.keyword(null, (String)"socketTimeout"), "datomic.ddbSocketTimeout"});
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"maxConnections"), 1024L, RT.keyword(null, (String)"maxErrorRetry"), 0L});

    public static Object invokeStatic(Object args) {
        Object object = args;
        args = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__6), (Object)const__11, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$ddb_client_args.invokeStatic(object2);
    }
}

