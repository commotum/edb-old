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

public final class ValueBackup$fn__20091$fn__20093
extends AFunction {
    Object backup_k;
    Object value_storage;
    Object buf;
    private static Class __cached_class__0;
    public static final Var const__0;

    public ValueBackup$fn__20091$fn__20093(Object object, Object object2, Object object3) {
        this.backup_k = object;
        this.value_storage = object2;
        this.buf = object3;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.value_storage;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Storage) {
                object = ((Storage)object2).store(this_.backup_k, this_.buf);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        ValueBackup$fn__20091$fn__20093 this_ = null;
        object = const__0.getRawRoot().invoke(object2, this_.backup_k, this_.buf);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"store");
    }
}

