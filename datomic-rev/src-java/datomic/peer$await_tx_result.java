/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.peer$await_tx_result$fn__21390;

public final class peer$await_tx_result
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"transaction-timeout");

    public static Object invokeStatic(Object prom) {
        Object object;
        Object result2;
        Object object2 = result2 = ((IFn)new peer$await_tx_result$fn__21390(prom)).invoke();
        result2 = null;
        if (Util.equiv((Object)prom, (Object)object2)) {
            object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)"Transaction timed out.");
        } else {
            object = prom;
            Object object3 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$await_tx_result.invokeStatic(object2);
    }
}

