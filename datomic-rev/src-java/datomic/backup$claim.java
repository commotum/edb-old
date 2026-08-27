/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class backup$claim
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object storage, Object id) {
        Object object;
        Object object2 = storage;
        storage = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof Storage) {
                Object object4 = id;
                id = null;
                object = ((Storage)object3).store("owner", ((IFn)const__1.getRawRoot()).invoke(object4));
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = id;
        id = null;
        object = const__0.getRawRoot().invoke(object3, (Object)"owner", ((IFn)const__1.getRawRoot()).invoke(object5));
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$claim.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"store");
        const__1 = RT.var((String)"datomic.io", (String)"string->bbuf");
    }
}

