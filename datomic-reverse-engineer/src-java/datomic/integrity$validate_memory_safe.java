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

public final class integrity$validate_memory_safe
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__1 = RT.var((String)"datomic.integrity", (String)"validate-log-cli");
    public static final Var const__2 = RT.var((String)"datomic.api", (String)"connect");
    public static final Var const__3 = RT.var((String)"datomic.integrity", (String)"crosscheck-log-cli");
    public static final Keyword const__4 = RT.keyword(null, (String)"uri");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        Object uri3 = ((IFn)const__0.getRawRoot()).invoke(object);
        ((IFn)const__1.getRawRoot()).invoke(uri3);
        ((IFn)const__2.getRawRoot()).invoke(uri3);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__4;
        Object object2 = uri3;
        uri3 = null;
        objectArray[1] = object2;
        return ((IFn)const__3.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_memory_safe.invokeStatic(object2);
    }
}

