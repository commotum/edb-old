/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$maybe_segset_storage
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"datomic.backup", (String)"segset-storage");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"latest-t");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__4 = RT.var((String)"datomic.backup", (String)"backup-seg-ids");

    public static Object invokeStatic(Object backup_storage) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.backupUseSegsetStorage");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            Object temp__5457__auto__20296;
            IFn iFn = (IFn)const__1.getRawRoot();
            Object object4 = temp__5457__auto__20296 = ((IFn)const__2.getRawRoot()).invoke(backup_storage);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = temp__5457__auto__20296;
                temp__5457__auto__20296 = null;
                Object prior_backup_t = object5;
                Object object6 = backup_storage;
                backup_storage = null;
                Object object7 = prior_backup_t;
                prior_backup_t = null;
                object3 = ((IFn)const__3.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__4.getRawRoot()).invoke(object6, object7));
            } else {
                object3 = null;
            }
            object = iFn.invoke(backup_storage, object3);
        } else {
            object = backup_storage;
            Object object8 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$maybe_segset_storage.invokeStatic(object2);
    }
}

