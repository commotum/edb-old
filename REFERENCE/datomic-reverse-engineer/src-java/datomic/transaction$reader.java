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

public final class transaction$reader
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"create-reader");
    public static final Var const__1 = RT.var((String)"datomic.transaction", (String)"read-handlers");

    public static Object invokeStatic(Object in) {
        Object object = in;
        in = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return transaction$reader.invokeStatic(object2);
    }
}

