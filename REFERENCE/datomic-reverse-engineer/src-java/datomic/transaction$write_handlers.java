/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.transaction$write_handlers$reify__15894;
import datomic.transaction$write_handlers$reify__15896;

public final class transaction$write_handlers
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.transaction", (String)"write-handlers");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__2 = RT.var((String)"datomic.fressian", (String)"user-write-handlers");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"common-write-handlers");
    public static final Object const__4 = RT.classForName((String)"datomic.db.DbId");
    public static final AFn const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 49, RT.keyword(null, (String)"column"), 9});
    public static final Object const__10 = RT.classForName((String)"datomic.db.Datum");
    public static final AFn const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 57, RT.keyword(null, (String)"column"), 9});

    public static Object invokeStatic(Object cache2) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__4;
        objectArray[1] = RT.mapUniqueKeys((Object[])new Object[]{"dbid", ((IObj)new transaction$write_handlers$reify__15894(null, cache2)).withMeta((IPersistentMap)const__9)});
        objectArray[2] = const__10;
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = "datum";
        Object object = cache2;
        cache2 = null;
        objectArray2[1] = ((IObj)new transaction$write_handlers$reify__15896(null, object)).withMeta((IPersistentMap)const__12);
        objectArray[3] = RT.mapUniqueKeys((Object[])objectArray2);
        return ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), const__3.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return transaction$write_handlers.invokeStatic(object2);
    }

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Boolean.TRUE);
    }

    public Object invoke() {
        return transaction$write_handlers.invokeStatic();
    }
}

