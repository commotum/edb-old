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
import java.util.Collection;

public final class aggregation$avg
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.aggregation", (String)"sum");

    public static Object invokeStatic(Object coll) {
        Object object = ((IFn)const__1.getRawRoot()).invoke(coll);
        Object object2 = coll;
        coll = null;
        return Numbers.divide((Object)object, (double)((Collection)object2).size());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$avg.invokeStatic(object2);
    }
}

