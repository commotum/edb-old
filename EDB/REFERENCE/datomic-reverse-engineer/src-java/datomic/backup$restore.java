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

public final class backup$restore
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"restore-db");
    public static final Keyword const__1 = RT.keyword(null, (String)"from-storage");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"create-storage");
    public static final Keyword const__3 = RT.keyword(null, (String)"t");
    public static final Keyword const__4 = RT.keyword(null, (String)"to-uri");
    public static final Var const__5 = RT.var((String)"datomic.backup", (String)"backup-concurrency");

    public static Object invokeStatic(Object from_storage_uri, Object to_uri, Object progress, Object t, Object incremental_QMARK_) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        objectArray[1] = ((IFn)const__2.getRawRoot()).invoke(from_storage_uri);
        objectArray[2] = const__3;
        Object object = t;
        t = null;
        objectArray[3] = object;
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__4;
        Object object2 = to_uri;
        to_uri = null;
        objectArray2[1] = object2;
        Object object3 = progress;
        progress = null;
        Object object4 = from_storage_uri;
        from_storage_uri = null;
        Object object5 = incremental_QMARK_;
        incremental_QMARK_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), (Object)RT.mapUniqueKeys((Object[])objectArray2), object3, ((IFn)const__5.getRawRoot()).invoke(object4), object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return backup$restore.invokeStatic(object6, object7, object8, object9, object10);
    }
}

