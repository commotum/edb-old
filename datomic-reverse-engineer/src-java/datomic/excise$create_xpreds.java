/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class excise$create_xpreds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.excise", (String)"create-es-pred");
    public static final Var const__2 = RT.var((String)"datomic.excise", (String)"create-as-pred");

    public static Object invokeStatic(Object db2, Object specs) {
        IPersistentVector iPersistentVector;
        Object object = ((IFn)const__0.getRawRoot()).invoke(specs);
        if (object != null && object != Boolean.FALSE) {
            Object object2 = ((IFn)const__1.getRawRoot()).invoke(db2, specs);
            Object object3 = db2;
            db2 = null;
            Object object4 = specs;
            specs = null;
            iPersistentVector = Tuple.create((Object)object2, (Object)((IFn)const__2.getRawRoot()).invoke(object3, object4));
        } else {
            iPersistentVector = null;
        }
        return iPersistentVector;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$create_xpreds.invokeStatic(object3, object4);
    }
}

