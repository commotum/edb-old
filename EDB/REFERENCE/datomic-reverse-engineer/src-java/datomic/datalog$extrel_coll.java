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
import datomic.datalog$extrel_coll$fn__18203;

public final class datalog$extrel_coll
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");

    public static Object invokeStatic(Object src, Object consts) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), consts);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = src;
            src = null;
        } else {
            Object object3 = consts;
            consts = null;
            Object object4 = src;
            src = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new datalog$extrel_coll$fn__18203(object3), object4);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$extrel_coll.invokeStatic(object3, object4);
    }
}

