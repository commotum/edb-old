/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.log.mem.Log;

public final class mem$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Keyword const__1 = RT.keyword(null, (String)"header");
    public static final Keyword const__2 = RT.keyword(null, (String)"body");

    public static Object invokeStatic(Object header, Object body) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        Object object = header;
        header = null;
        objectArray[1] = object;
        objectArray[2] = const__2;
        Object object2 = body;
        body = null;
        objectArray[3] = object2;
        return new Log(((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return mem$create.invokeStatic(object3, object4);
    }

    public static Object invokeStatic() {
        return new Log(((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY));
    }

    public Object invoke() {
        return mem$create.invokeStatic();
    }
}

