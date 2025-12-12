/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.MemLog;
import datomic.log.TailTxIter;

public final class log$fn__16476
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"key-comparator");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"log-key");
    public static final Var const__2 = RT.var((String)"datomic.log", (String)"binary-search");

    public static Object invokeStatic(Object this_, Object k) {
        TailTxIter tailTxIter;
        Object idx;
        Object comp2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        Object object = k;
        k = null;
        Object object2 = comp2;
        comp2 = null;
        Object object3 = idx = ((IFn)const__2.getRawRoot()).invoke(((MemLog)this_).txes, object, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            this_ = null;
            idx = null;
            tailTxIter = new TailTxIter(((MemLog)this_).txes, RT.longCast((Object)((Number)idx)));
        } else {
            tailTxIter = null;
        }
        return tailTxIter;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$fn__16476.invokeStatic(object3, object4);
    }
}

