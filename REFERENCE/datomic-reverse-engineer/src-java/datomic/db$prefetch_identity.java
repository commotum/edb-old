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
import datomic.db$prefetch_identity$fn__13992;

public final class db$prefetch_identity
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"prefetch!");

    public static Object invokeStatic(Object dispatcher, Object tx_stat_registers, Object db2, Object e, Object a, Object v) {
        Object object = dispatcher;
        dispatcher = null;
        Object object2 = v;
        v = null;
        Object object3 = tx_stat_registers;
        tx_stat_registers = null;
        Object object4 = db2;
        db2 = null;
        Object object5 = e;
        e = null;
        Object object6 = a;
        a = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)new db$prefetch_identity$fn__13992(object2, object3, object4, object5, object6));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return db$prefetch_identity.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

