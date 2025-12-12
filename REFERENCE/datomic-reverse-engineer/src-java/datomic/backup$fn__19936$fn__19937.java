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

public final class backup$fn__19936$fn__19937
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"thread-pool");
    public static final Keyword const__1 = RT.keyword(null, (String)"nthreads");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"backup-branch-size");
    public static final Keyword const__3 = RT.keyword(null, (String)"name");

    public Object invoke() {
        backup$fn__19936$fn__19937 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2.getRawRoot(), const__3, "backup-branch"}));
    }
}

