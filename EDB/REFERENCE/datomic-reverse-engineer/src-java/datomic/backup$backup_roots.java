/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class backup$backup_roots
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Var const__5;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object job, Object t, Object to_storage) {
        Object object;
        Object object2 = to_storage;
        to_storage = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof Storage) {
                Object object4 = t;
                t = null;
                Object object5 = job;
                job = null;
                object = ((Storage)object3).store(((IFn)const__1.getRawRoot()).invoke(object4), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object5), (Object)const__4, const__5.getRawRoot()));
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object6 = t;
        t = null;
        Object object7 = job;
        job = null;
        object = const__0.getRawRoot().invoke(object3, ((IFn)const__1.getRawRoot()).invoke(object6), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object7), (Object)const__4, const__5.getRawRoot()));
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return backup$backup_roots.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"store");
        const__1 = RT.var((String)"datomic.backup", (String)"roots-path");
        const__2 = RT.var((String)"datomic.fressian", (String)"byte-buf");
        const__3 = RT.var((String)"datomic.backup", (String)"mem->backup");
        const__4 = RT.keyword(null, (String)"handlers");
        const__5 = RT.var((String)"datomic.fressian", (String)"user-write-handlers");
    }
}

