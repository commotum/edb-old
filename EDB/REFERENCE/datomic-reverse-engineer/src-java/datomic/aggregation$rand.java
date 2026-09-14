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
import datomic.aggregation$rand$fn__17104;
import java.util.List;

public final class aggregation$rand
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"rand-int");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"repeatedly");

    public static Object invokeStatic(Object n, Object coll) {
        Object object = n;
        n = null;
        Object object2 = coll;
        coll = null;
        return ((IFn)const__1.getRawRoot()).invoke(object, (Object)new aggregation$rand$fn__17104(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aggregation$rand.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object coll) {
        List list = (List)coll;
        Object object = coll;
        coll = null;
        return list.get(RT.intCast((Object)((Number)((IFn)const__0.getRawRoot()).invoke((Object)((List)object).size()))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$rand.invokeStatic(object2);
    }
}

