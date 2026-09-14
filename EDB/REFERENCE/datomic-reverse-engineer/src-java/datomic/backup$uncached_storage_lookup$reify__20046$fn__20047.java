/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class backup$uncached_storage_lookup$reify__20046$fn__20047
extends AFunction {
    Object storage;
    Object k;
    private static Class __cached_class__0;
    public static final Var const__0;

    public backup$uncached_storage_lookup$reify__20046$fn__20047(Object object, Object object2) {
        this.storage = object;
        this.k = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.storage;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Storage) {
                object = ((Storage)object2).retrieve(this_.k);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        backup$uncached_storage_lookup$reify__20046$fn__20047 this_ = null;
        object = const__0.getRawRoot().invoke(object2, this_.k);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"retrieve");
    }
}

