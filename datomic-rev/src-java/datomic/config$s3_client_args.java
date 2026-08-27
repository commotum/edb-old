/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class config$s3_client_args
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"s3-client-args");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"property-map");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"clientExecutionTimeout"), "datomic.s3ClientExecutionTimeout", RT.keyword(null, (String)"connectionTimeout"), "datomic.s3ConnectionTimeout", RT.keyword(null, (String)"requestTimeout"), "datomic.s3RequestTimeout", RT.keyword(null, (String)"socketTimeout"), "datomic.s3SocketTimeout"});
    public static final Keyword const__8 = RT.keyword(null, (String)"maxConnections");

    public static Object invokeStatic(Object args) {
        Object object = args;
        args = null;
        return ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__7), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__8, Numbers.num((long)Numbers.multiply((long)64L, (long)1024L))}), object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$s3_client_args.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(null);
    }

    public Object invoke() {
        return config$s3_client_args.invokeStatic();
    }
}

