/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class kv_sql_ext$kv_sql
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"resolve");
    public static final AFn const__1 = (AFn)Symbol.intern((String)"datomic.kv-sql", (String)"from-spec");
    public static final Var const__2 = RT.var((String)"datomic.kv-sql-ext", (String)"cluster-conf->spec");

    public static Object invokeStatic(Object cluster_conf) {
        Object object = cluster_conf;
        cluster_conf = null;
        return ((IFn)((IFn)const__0.getRawRoot()).invoke((Object)const__1)).invoke(((IFn)const__2.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql_ext$kv_sql.invokeStatic(object2);
    }
}

