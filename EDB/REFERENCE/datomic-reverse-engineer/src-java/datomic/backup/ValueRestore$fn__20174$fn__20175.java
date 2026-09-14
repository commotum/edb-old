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
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;

public final class ValueRestore$fn__20174$fn__20175
extends AFunction {
    Object value_storage;
    Object k__GT_backup_k;
    Object k;
    private static Class __cached_class__0;
    public static final Var const__0;

    public ValueRestore$fn__20174$fn__20175(Object object, Object object2, Object object3) {
        this.value_storage = object;
        this.k__GT_backup_k = object2;
        this.k = object3;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.value_storage;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Storage) {
                object = ((Storage)object2).retrieve(((IFn)this_.k__GT_backup_k).invoke(this_.k));
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        ValueRestore$fn__20174$fn__20175 this_ = null;
        object = const__0.getRawRoot().invoke(object2, ((IFn)this_.k__GT_backup_k).invoke(this_.k));
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"retrieve");
    }
}

