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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogDir;

public final class integrity$log_storage_seq$branch_QMARK___22519
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");

    public Object invoke(Object p__22518) {
        Object seg;
        Object map__22520;
        Object object;
        Object object2 = p__22518;
        p__22518 = null;
        Object map__225202 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__225202);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__225202;
            map__225202 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__225202;
            map__225202 = null;
        }
        Object object5 = map__22520 = object;
        map__22520 = null;
        Object object6 = seg = RT.get((Object)object5, (Object)const__3);
        seg = null;
        return ((IFn)const__6.getRawRoot()).invoke(object6) instanceof LogDir ? Boolean.TRUE : Boolean.FALSE;
    }
}

