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

public final class kv_cache$start_kv_cache$record__10071
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"CacheStackRepair");
    public static final Object const__2 = 1L;

    public Object invoke(Object _) {
        kv_cache$start_kv_cache$record__10071 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, const__2);
    }
}

