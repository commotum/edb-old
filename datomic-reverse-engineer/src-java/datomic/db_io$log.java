/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db_io$log
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__4 = RT.keyword(null, (String)"olookup");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"find-log");

    public static Object invokeStatic(Object p__17062) {
        Object object;
        Object object2 = p__17062;
        p__17062 = null;
        Object map__17063 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__17063);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__17063;
            map__17063 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__17063;
            map__17063 = null;
        }
        Object map__170632 = object;
        Object cluster2 = RT.get((Object)map__170632, (Object)const__3);
        Object object5 = map__170632;
        map__170632 = null;
        Object olookup = RT.get((Object)object5, (Object)const__4);
        Object object6 = cluster2;
        cluster2 = null;
        Object object7 = olookup;
        olookup = null;
        return ((IFn)const__5.getRawRoot()).invoke(object6, object7);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db_io$log.invokeStatic(object2);
    }
}

