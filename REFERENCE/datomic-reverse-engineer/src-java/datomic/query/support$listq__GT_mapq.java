/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query.support$listq__GT_mapq$fn__19068;

public final class support$listq__GT_mapq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__2 = 2L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"partition-by");
    public static final AFn const__12 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"find"), RT.keyword(null, (String)"where"), RT.keyword(null, (String)"syms"), RT.keyword(null, (String)"keys"), RT.keyword(null, (String)"with"), RT.keyword(null, (String)"timeout"), RT.keyword(null, (String)"strs"), RT.keyword(null, (String)"in")});

    public static Object invokeStatic(Object lq) {
        Object object = lq;
        lq = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new support$listq__GT_mapq$fn__19068(), (Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke(const__2, ((IFn)const__3.getRawRoot()).invoke((Object)const__12, object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return support$listq__GT_mapq.invokeStatic(object2);
    }
}

