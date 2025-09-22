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

public final class backup$backup_seg_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"create-restore-job");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"lookup");
    public static final Keyword const__5 = RT.keyword(null, (String)"index-top-node");
    public static final Keyword const__6 = RT.keyword(null, (String)"log-root-node");
    public static final Var const__7 = RT.var((String)"datomic.treewalk", (String)"db-seq");

    public static Object invokeStatic(Object backup_storage, Object t) {
        Object log_root_node;
        Object object;
        Object object2 = backup_storage;
        backup_storage = null;
        Object object3 = t;
        t = null;
        Object map__20292 = ((IFn)const__0.getRawRoot()).invoke(object2, object3);
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20292);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__20292;
            map__20292 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object5)));
        } else {
            object = map__20292;
            map__20292 = null;
        }
        Object map__202922 = object;
        Object lookup = RT.get((Object)map__202922, (Object)const__4);
        Object index_top_node = RT.get((Object)map__202922, (Object)const__5);
        Object object6 = map__202922;
        map__202922 = null;
        Object object7 = log_root_node = RT.get((Object)object6, (Object)const__6);
        log_root_node = null;
        Object object8 = index_top_node;
        index_top_node = null;
        Object object9 = lookup;
        lookup = null;
        return ((IFn)const__7.getRawRoot()).invoke(object7, object8, object9);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$backup_seg_ids.invokeStatic(object3, object4);
    }
}

