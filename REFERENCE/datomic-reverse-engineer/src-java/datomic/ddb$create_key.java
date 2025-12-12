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

public final class ddb$create_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"number?");
    public static final Keyword const__1 = RT.keyword(null, (String)"n");
    public static final Keyword const__2 = RT.keyword(null, (String)"s");

    public static Object invokeStatic(Object k) {
        Object[] objectArray = new Object[2];
        objectArray[0] = "id";
        Object[] objectArray2 = new Object[2];
        Object object = ((IFn)const__0.getRawRoot()).invoke(k);
        objectArray2[0] = object != null && object != Boolean.FALSE ? const__1 : const__2;
        Object object2 = k;
        k = null;
        objectArray2[1] = object2;
        objectArray[1] = RT.mapUniqueKeys((Object[])objectArray2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$create_key.invokeStatic(object2);
    }
}

