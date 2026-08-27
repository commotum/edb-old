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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$raet$fn__17858;

public final class stats$raet
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"index-summary");
    public static final Keyword const__1 = RT.keyword(null, (String)"raet");

    public static Object invokeStatic(Object db2, Object index2) {
        Object object = db2;
        Object object2 = index2;
        index2 = null;
        Object object3 = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)const__1, (Object)new stats$raet$fn__17858(object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return stats$raet.invokeStatic(object3, object4);
    }
}

