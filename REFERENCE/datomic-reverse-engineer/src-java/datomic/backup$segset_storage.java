/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class backup$segset_storage
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"set?"), Symbol.intern(null, (String)"seg-id-set")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 28}));
    public static final Var const__4 = RT.var((String)"datomic.backup", (String)"->SegSetStorage");

    public static Object invokeStatic(Object backup_storage, Object seg_id_set) {
        Object object = seg_id_set;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = ((IFn)const__0.getRawRoot()).invoke(seg_id_set);
            if (object2 != null && object2 != Boolean.FALSE) {
            } else {
                throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
            }
        }
        Object object3 = backup_storage;
        backup_storage = null;
        Object object4 = seg_id_set;
        seg_id_set = null;
        return ((IFn)const__4.getRawRoot()).invoke(object3, object4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$segset_storage.invokeStatic(object3, object4);
    }
}

