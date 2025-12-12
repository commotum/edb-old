/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$attr_hook_attr_ids$fn__13050;

public final class db$attr_hook_attr_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"identity");
    public static final AFn const__16 = (AFn)RT.vector((Object[])new Object[]{42L, 41L, 40L, 43L, 51L, 45L, 44L, RT.keyword((String)"db.attr", (String)"preds"), RT.keyword((String)"db", (String)"tupleType"), RT.keyword((String)"db", (String)"tupleTypes"), RT.keyword((String)"db", (String)"tupleAttrs")});

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new db$attr_hook_attr_ids$fn__13050(object)), ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot())), (Object)const__16);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$attr_hook_attr_ids.invokeStatic(object2);
    }
}

