/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class ValueBackup$fn__20107$fn__20108$fn__20109
extends AFunction {
    Object backup_leaf_id;
    Object value_storage;
    private static Class __cached_class__0;
    public static final Var const__0;

    public ValueBackup$fn__20107$fn__20108$fn__20109(Object object, Object object2) {
        this.backup_leaf_id = object;
        this.value_storage = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.value_storage;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Storage) {
                object = ((Storage)object2).exists_QMARK_(this_.backup_leaf_id);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        ValueBackup$fn__20107$fn__20108$fn__20109 this_ = null;
        object = const__0.getRawRoot().invoke(object2, this_.backup_leaf_id);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"exists?");
    }
}

