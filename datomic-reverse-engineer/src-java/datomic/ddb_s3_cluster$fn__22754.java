/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.ddb_s3_cluster$fn__22754$fn__22755;

public final class ddb_s3_cluster$fn__22754
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.ddb-s3-cluster", (String)"efs-write-pool");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__22758;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__4);
        Var var3 = v__6457__auto__22758 = var2;
        v__6457__auto__22758 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__5);
            var = var4;
            var4.bindRoot((Object)new Delay((IFn)new ddb_s3_cluster$fn__22754$fn__22755()));
        }
        return var;
    }

    public Object invoke() {
        return ddb_s3_cluster$fn__22754.invokeStatic();
    }
}

