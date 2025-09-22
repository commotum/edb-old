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
import datomic.integrity$index_storage_seq$children__22536$fn__22538;
import datomic.integrity$index_storage_seq$children__22536$fn__22540;

public final class integrity$index_storage_seq$children__22536
extends AFunction {
    Object olookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");

    public integrity$index_storage_seq$children__22536(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object p__22535) {
        Object object;
        integrity$index_storage_seq$children__22536 this_;
        Object map__22537;
        Object object2;
        Object object3 = p__22535;
        p__22535 = null;
        Object map__225372 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__225372);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__225372;
            map__225372 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__225372;
            map__225372 = null;
        }
        Object object6 = map__22537 = object2;
        map__22537 = null;
        Object seg = RT.get((Object)object6, (Object)const__3);
        if (seg instanceof RootNode) {
            Object object7 = seg;
            seg = null;
            this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)new integrity$index_storage_seq$children__22536$fn__22538(this_.olookup), ((RootNode)object7).dirids);
        } else if (seg instanceof DirNode) {
            Object object8 = seg;
            seg = null;
            this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)new integrity$index_storage_seq$children__22536$fn__22540(this_.olookup), ((DirNode)object8).segids);
        } else {
            object = null;
        }
        return object;
    }
}

