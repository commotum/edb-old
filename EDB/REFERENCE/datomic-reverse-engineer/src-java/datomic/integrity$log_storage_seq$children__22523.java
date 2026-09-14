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
import datomic.integrity$log_storage_seq$children__22523$fn__22525;

public final class integrity$log_storage_seq$children__22523
extends AFunction {
    Object olookup;
    Object root_id;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");
    public static final Keyword const__4 = RT.keyword(null, (String)"seg");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");

    public integrity$log_storage_seq$children__22523(Object object, Object object2) {
        this.olookup = object;
        this.root_id = object2;
    }

    public Object invoke(Object p__22522) {
        Object object;
        Object object2 = p__22522;
        p__22522 = null;
        Object map__22524 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__22524);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__22524;
            map__22524 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__22524;
            map__22524 = null;
        }
        Object map__225242 = object;
        Object uuid = RT.get((Object)map__225242, (Object)const__3);
        Object object5 = map__225242;
        map__225242 = null;
        Object seg = RT.get((Object)object5, (Object)const__4);
        Object object6 = uuid;
        uuid = null;
        Object object7 = seg;
        seg = null;
        integrity$log_storage_seq$children__22523 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke((Object)new integrity$log_storage_seq$children__22523$fn__22525(this_.olookup, this_.root_id, object6), object7);
    }
}

