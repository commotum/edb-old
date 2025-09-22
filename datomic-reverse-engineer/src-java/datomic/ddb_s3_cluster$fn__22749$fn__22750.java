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

public final class ddb_s3_cluster$fn__22749$fn__22750
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"cached-thread-pool");
    public static final AFn const__2 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), "efs-read"});

    public Object invoke() {
        ddb_s3_cluster$fn__22749$fn__22750 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__2);
    }
}

