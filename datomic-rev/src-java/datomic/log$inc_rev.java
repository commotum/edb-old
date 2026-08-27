/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class log$inc_rev
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__2 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"rev"));
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"inc");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__2, const__3.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$inc_rev.invokeStatic(object2);
    }
}

