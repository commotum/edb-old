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

public final class query$process_self_unifications
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"has-self-unifications?");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"rename-self-unifications");
    public static final Var const__2 = RT.var((String)"datomic.query", (String)"gensym-padding");

    public static Object invokeStatic(Object qmap) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(qmap);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = qmap;
            Object object4 = qmap;
            qmap = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, ((IFn)const__2.getRawRoot()).invoke(object4));
        } else {
            object = qmap;
            Object object5 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$process_self_unifications.invokeStatic(object2);
    }
}

