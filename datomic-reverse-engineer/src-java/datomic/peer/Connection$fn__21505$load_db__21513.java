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
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class Connection$fn__21505$load_db__21513
extends AFunction {
    Object cluster_conf;
    Object olookup;
    Object cluster;
    public static final Var const__0 = RT.var((String)"datomic.db-io", (String)"load-db-from-basis");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"db-id");

    public Connection$fn__21505$load_db__21513(Object object, Object object2, Object object3) {
        this.cluster_conf = object;
        this.olookup = object2;
        this.cluster = object3;
    }

    public Object invoke(Object basis_db) {
        Object object = basis_db;
        basis_db = null;
        Connection$fn__21505$load_db__21513 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cluster, this_.olookup, ((IFn)const__1.getRawRoot()).invoke(this_.cluster_conf, (Object)const__2), object);
    }
}

