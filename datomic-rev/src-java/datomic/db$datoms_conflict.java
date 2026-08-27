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

public final class db$datoms_conflict
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"argd");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"datoms-conflict");
    public static final Keyword const__2 = RT.keyword(null, (String)"d1");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"datom-error-desc");
    public static final Keyword const__4 = RT.keyword(null, (String)"d2");

    public static Object invokeStatic(Object db2, Object d1, Object d2) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__2;
        Object object = d1;
        d1 = null;
        objectArray[1] = ((IFn)const__3.getRawRoot()).invoke(db2, object);
        objectArray[2] = const__4;
        Object object2 = db2;
        db2 = null;
        Object object3 = d2;
        d2 = null;
        objectArray[3] = ((IFn)const__3.getRawRoot()).invoke(object2, object3);
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)"Two datoms in the same transaction conflict", (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$datoms_conflict.invokeStatic(object4, object5, object6);
    }
}

