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

public final class integrity$pod_storage_seq$fn__22551
extends AFunction {
    Object mkv;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"prev");

    public integrity$pod_storage_seq$fn__22551(Object object) {
        this.mkv = object;
    }

    public Object invoke(Object p__22550) {
        Object object;
        Object prev;
        Object map__22552;
        Object object2;
        Object object3 = p__22550;
        p__22550 = null;
        Object map__225522 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__225522);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__225522;
            map__225522 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__225522;
            map__225522 = null;
        }
        Object object6 = map__22552 = object2;
        map__22552 = null;
        Object object7 = prev = RT.get((Object)object6, (Object)const__3);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = prev;
            prev = null;
            integrity$pod_storage_seq$fn__22551 this_ = null;
            object = ((IFn)this_.mkv).invoke(object8);
        } else {
            object = null;
        }
        return object;
    }
}

