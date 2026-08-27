/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cache$fn__9374$fn__9375
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"thread-pool");
    public static final Keyword const__1 = RT.keyword(null, (String)"nthreads");
    public static final Var const__3 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__5 = RT.keyword(null, (String)"name");

    public Object invoke() {
        cache$fn__9374$fn__9375 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, Numbers.max((Object)((IFn)const__3.getRawRoot()).invoke((Object)"datomic.readAheadPool"), (long)1L), const__5, "read-ahead"}));
    }
}

