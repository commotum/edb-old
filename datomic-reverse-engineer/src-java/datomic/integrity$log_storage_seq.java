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
import datomic.integrity$log_storage_seq$branch_QMARK___22519;
import datomic.integrity$log_storage_seq$children__22523;

public final class integrity$log_storage_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"connection-resources");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__6 = RT.keyword(null, (String)"olookup");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"root-id");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"tree-seq");
    public static final Keyword const__9 = RT.keyword(null, (String)"type");
    public static final Keyword const__10 = RT.keyword(null, (String)"root");
    public static final Keyword const__11 = RT.keyword(null, (String)"uuid");
    public static final Keyword const__12 = RT.keyword(null, (String)"seg");

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object uri3;
        Object object2 = uri2;
        uri2 = null;
        Object object3 = uri3 = ((IFn)const__0.getRawRoot()).invoke(object2);
        uri3 = null;
        Object map__22517 = ((IFn)const__1.getRawRoot()).invoke(object3);
        Object object4 = ((IFn)const__2.getRawRoot()).invoke(map__22517);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__22517;
            map__22517 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object5)));
        } else {
            object = map__22517;
            map__22517 = null;
        }
        Object map__225172 = object;
        Object cluster2 = RT.get((Object)map__225172, (Object)const__5);
        Object object6 = map__225172;
        map__225172 = null;
        Object olookup = RT.get((Object)object6, (Object)const__6);
        Object object7 = cluster2;
        cluster2 = null;
        Object root_id2 = ((IFn)const__7.getRawRoot()).invoke(object7);
        integrity$log_storage_seq$branch_QMARK___22519 branch_QMARK_ = new integrity$log_storage_seq$branch_QMARK___22519();
        integrity$log_storage_seq$children__22523 children = new integrity$log_storage_seq$children__22523(olookup, root_id2);
        Object object8 = olookup;
        olookup = null;
        Object root = RT.get((Object)object8, (Object)root_id2);
        integrity$log_storage_seq$branch_QMARK___22519 integrity$log_storage_seq$branch_QMARK___22519 = branch_QMARK_;
        branch_QMARK_ = null;
        integrity$log_storage_seq$children__22523 integrity$log_storage_seq$children__22523 = children;
        children = null;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__9;
        objectArray[1] = const__10;
        objectArray[2] = const__11;
        Object object9 = root_id2;
        root_id2 = null;
        objectArray[3] = object9;
        objectArray[4] = const__12;
        Object object10 = root;
        root = null;
        objectArray[5] = object10;
        return ((IFn)const__8.getRawRoot()).invoke((Object)integrity$log_storage_seq$branch_QMARK___22519, (Object)integrity$log_storage_seq$children__22523, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$log_storage_seq.invokeStatic(object2);
    }
}

