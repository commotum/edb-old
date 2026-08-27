/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$gc_deleted_db$fn__19887
extends AFunction {
    int c;
    Object db_cluster;
    Object status;
    public static final Var const__1 = RT.var((String)"datomic.garbage", (String)"gc-delete-vals");

    public garbage$gc_deleted_db$fn__19887(int n, Object object, Object object2) {
        this.c = n;
        this.db_cluster = object;
        this.status = object2;
    }

    public Object invoke(Object n, Object ks) {
        Object object = n;
        n = null;
        Object object2 = ks;
        ks = null;
        Number n2 = Numbers.add((Object)object, (Object)((IFn)const__1.getRawRoot()).invoke(this.db_cluster, object2));
        ((IFn)this.status).invoke((Object)"Deleted ", (Object)n2, (Object)" of ", (Object)this.c, (Object)" index segments");
        Object var3_3 = null;
        return n2;
    }
}

