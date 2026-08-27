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

public final class transaction$writer
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.transaction", (String)"writer");
    public static final Var const__1 = RT.var((String)"datomic.fressian", (String)"create-writer");
    public static final Var const__2 = RT.var((String)"datomic.transaction", (String)"write-handlers");

    public static Object invokeStatic(Object out, Object cache2) {
        Object object = out;
        out = null;
        Object object2 = cache2;
        cache2 = null;
        return ((IFn)const__1.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return transaction$writer.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object out) {
        Object object = out;
        out = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Boolean.TRUE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return transaction$writer.invokeStatic(object2);
    }
}

