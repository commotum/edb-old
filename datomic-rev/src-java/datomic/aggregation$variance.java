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
import datomic.aggregation$variance$fn__17100;
import java.util.Collection;

public final class aggregation$variance
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.aggregation", (String)"avg");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"datomic.aggregation", (String)"sum");

    public static Object invokeStatic(Object coll) {
        Object xs;
        Object av;
        Object object = av = ((IFn)const__0.getRawRoot()).invoke(coll);
        av = null;
        Object object2 = xs = ((IFn)const__1.getRawRoot()).invoke((Object)new aggregation$variance$fn__17100(object), coll);
        xs = null;
        Object object3 = coll;
        coll = null;
        return Numbers.divide((Object)((IFn)const__3.getRawRoot()).invoke(object2), (long)((Collection)object3).size());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$variance.invokeStatic(object2);
    }
}

