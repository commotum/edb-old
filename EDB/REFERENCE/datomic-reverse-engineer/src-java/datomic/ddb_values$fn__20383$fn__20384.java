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

public final class ddb_values$fn__20383$fn__20384
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"thread-pool");
    public static final Keyword const__1 = RT.keyword(null, (String)"nthreads");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__3 = RT.keyword(null, (String)"name");

    public Object invoke() {
        ddb_values$fn__20383$fn__20384 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.writeConcurrency"), const__3, "ddb-chunk"}));
    }
}

