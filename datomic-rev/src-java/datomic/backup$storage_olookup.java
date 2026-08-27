/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
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

public final class backup$storage_olookup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"lookup-transformer");
    public static final Var const__1 = RT.var((String)"datomic.domain", (String)"peer-object-lookup");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"uncached-storage-lookup");
    public static final Var const__3 = RT.var((String)"datomic.domain", (String)"common-read-handlers");
    public static final Var const__4 = RT.var((String)"datomic.domain", (String)"system-cache");
    public static final Keyword const__5 = RT.keyword(null, (String)"key-fn");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__7 = RT.var((String)"datomic.backup", (String)"backup-k-factory");
    public static final Var const__8 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");

    public static Object invokeStatic(Object storage, Object backup_version) {
        Object object = storage;
        storage = null;
        Object object2 = backup_version;
        backup_version = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object), const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke()), (Object)const__5, ((IFn)const__6.getRawRoot()).invoke(((IFn.LO)const__7.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object2))), const__8.getRawRoot()));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$storage_olookup.invokeStatic(object3, object4);
    }
}

