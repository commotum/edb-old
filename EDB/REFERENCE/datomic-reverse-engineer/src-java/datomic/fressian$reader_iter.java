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
import datomic.fressian.FressianIter;

public final class fressian$reader_iter
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"create-reader");

    public static Object invokeStatic(Object is, Object handlers) {
        Object object = is;
        is = null;
        Object object2 = handlers;
        handlers = null;
        return new FressianIter(((IFn)const__0.getRawRoot()).invoke(object, object2), null).next();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$reader_iter.invokeStatic(object3, object4);
    }
}

