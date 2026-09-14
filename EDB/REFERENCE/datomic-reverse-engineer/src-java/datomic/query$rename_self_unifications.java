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
import datomic.query$rename_self_unifications$fn__19400;

public final class query$rename_self_unifications
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__2 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"where"));

    public static Object invokeStatic(Object qmap, Object padding) {
        Object object = qmap;
        qmap = null;
        Object object2 = padding;
        padding = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__2, (Object)new query$rename_self_unifications$fn__19400(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$rename_self_unifications.invokeStatic(object3, object4);
    }
}

