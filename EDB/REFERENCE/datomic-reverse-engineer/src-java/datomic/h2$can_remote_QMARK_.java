/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class h2$can_remote_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"storage-access");
    public static final Keyword const__4 = RT.keyword(null, (String)"storage-datomic-password");
    public static final Keyword const__5 = RT.keyword(null, (String)"storage-admin-password");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(Object p__11633) {
        Boolean bl;
        Object object;
        Object object2 = p__11633;
        p__11633 = null;
        Object map__11634 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__11634);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__11634;
            map__11634 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__11634;
            map__11634 = null;
        }
        Object map__116342 = object;
        Object storage_access = RT.get((Object)map__116342, (Object)const__3);
        Object storage_datomic_password = RT.get((Object)map__116342, (Object)const__4);
        Object object5 = map__116342;
        map__116342 = null;
        Object storage_admin_password = RT.get((Object)object5, (Object)const__5);
        Object object6 = storage_access;
        storage_access = null;
        if (Util.equiv((Object)"remote", (Object)object6)) {
            Object object7;
            Object and__5236__auto__11636;
            Object object8 = storage_datomic_password;
            storage_datomic_password = null;
            Object object9 = and__5236__auto__11636 = object8;
            if (object9 != null && object9 != Boolean.FALSE) {
                object7 = storage_admin_password;
                storage_admin_password = null;
            } else {
                object7 = and__5236__auto__11636;
                and__5236__auto__11636 = null;
            }
            if (object7 == null || object7 == Boolean.FALSE) throw (Throwable)new IllegalArgumentException("You must set storage-datomic-password and storage-admin-password before enabling storage-access=remote.");
            bl = Boolean.TRUE;
            return bl;
        } else {
            bl = Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$can_remote_QMARK_.invokeStatic(object2);
    }
}

