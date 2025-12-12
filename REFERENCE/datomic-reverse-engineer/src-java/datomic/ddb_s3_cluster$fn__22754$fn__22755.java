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

public final class ddb_s3_cluster$fn__22754$fn__22755
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"handoff-thread-pool");
    public static final Keyword const__1 = RT.keyword(null, (String)"core-threads");
    public static final Object const__2 = 2L;
    public static final Keyword const__3 = RT.keyword(null, (String)"max-threads");
    public static final Var const__4 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__5 = RT.keyword(null, (String)"name");

    public Object invoke() {
        ddb_s3_cluster$fn__22754$fn__22755 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"datomic.efsWritePool"), const__5, "efs-write"}));
    }
}

