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
import datomic.index.DirNode;
import datomic.index.RootNode;

public final class integrity$index_storage_seq$branch_QMARK___22531
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg");

    public Object invoke(Object p__22530) {
        Boolean bl;
        Object map__22532;
        Object object;
        Object object2 = p__22530;
        p__22530 = null;
        Object map__225322 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__225322);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__225322;
            map__225322 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__225322;
            map__225322 = null;
        }
        Object object5 = map__22532 = object;
        map__22532 = null;
        Object seg = RT.get((Object)object5, (Object)const__3);
        boolean or__5238__auto__22534 = seg instanceof RootNode;
        if (or__5238__auto__22534) {
            bl = or__5238__auto__22534 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object6 = seg;
            seg = null;
            bl = object6 instanceof DirNode ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

