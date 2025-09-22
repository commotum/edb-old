/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.garbage$gc_deleted_dbs$fn__19903;

public final class garbage$gc_deleted_dbs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uri");
    public static final Var const__4 = RT.var((String)"datomic.garbage", (String)"install-mark-handler");
    public static final Var const__5 = RT.var((String)"datomic.garbage", (String)"get-db-ids");
    public static final Keyword const__6 = RT.keyword(null, (String)"deleted");
    public static final Keyword const__7 = RT.keyword(null, (String)"active");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__10 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__11 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Object const__12 = 0L;
    public static final Var const__14 = RT.var((String)"datomic.catalog", (String)"remove-deleted-database");
    public static final Var const__15 = RT.var((String)"datomic.garbage", (String)"deleted-db-cluster-conf");
    public static final Var const__16 = RT.var((String)"datomic.coordination", (String)"create-db-cluster");
    public static final Var const__17 = RT.var((String)"datomic.garbage", (String)"gc-deleted-db");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__28 = RT.var((String)"datomic.garbage", (String)"garbage-agent");

    public static Object invokeStatic(Object p__19895) {
        Object object;
        Object object2;
        Object map__19896;
        Object object3;
        Object object4 = p__19895;
        p__19895 = null;
        Object map__198962 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__198962);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__198962;
            map__198962 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__198962;
            map__198962 = null;
        }
        Object object7 = map__19896 = object3;
        map__19896 = null;
        Object uri2 = RT.get((Object)object7, (Object)const__3);
        ((IFn)const__4.getRawRoot()).invoke();
        Object map__19897 = ((IFn)const__5.getRawRoot()).invoke(uri2);
        Object object8 = ((IFn)const__0.getRawRoot()).invoke(map__19897);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__19897;
            map__19897 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object9)));
        } else {
            object2 = map__19897;
            map__19897 = null;
        }
        Object map__198972 = object2;
        Object deleted = RT.get((Object)map__198972, (Object)const__6);
        Object object10 = map__198972;
        map__198972 = null;
        Object active = RT.get((Object)object10, (Object)const__7);
        Object object11 = ((IFn)const__1.getRawRoot()).invoke(deleted);
        if (object11 != null && object11 != Boolean.FALSE) {
            Object n;
            Object cluster_conf;
            ((IFn)const__8.getRawRoot()).invoke((Object)"Deleting storage for", (Object)RT.count((Object)deleted), (Object)"deleted dbs.");
            Object object12 = cluster_conf = ((IFn)const__10.getRawRoot()).invoke(uri2);
            cluster_conf = null;
            Object system_cluster2 = ((IFn)const__11.getRawRoot()).invoke(object12);
            Object object13 = deleted;
            deleted = null;
            Object seq_19898 = ((IFn)const__1.getRawRoot()).invoke(object13);
            Object chunk_19899 = null;
            long count_19900 = 0L;
            long i_19901 = 0L;
            while (true) {
                Object temp__5457__auto__19928;
                if (i_19901 < count_19900) {
                    Object id = ((Indexed)chunk_19899).nth(RT.intCast((long)i_19901));
                    Object object14 = RT.get((Object)active, (Object)id);
                    if (object14 != null && object14 != Boolean.FALSE) {
                        ((IFn)const__8.getRawRoot()).invoke((Object)"Database has been restored, skipping ", id);
                        Object object15 = id;
                        id = null;
                        ((IFn)const__14.getRawRoot()).invoke(system_cluster2, object15);
                    } else {
                        Object db_cluster;
                        Object db_cluster_conf;
                        ((IFn)const__8.getRawRoot()).invoke((Object)"Deleting storage for ", id);
                        Object object16 = id;
                        id = null;
                        Object object17 = db_cluster_conf = ((IFn)const__15.getRawRoot()).invoke(uri2, object16);
                        db_cluster_conf = null;
                        Object object18 = db_cluster = ((IFn)const__16.getRawRoot()).invoke(object17);
                        db_cluster = null;
                        ((IFn)const__17.getRawRoot()).invoke(system_cluster2, object18, const__8.getRawRoot());
                    }
                    Object object19 = seq_19898;
                    seq_19898 = null;
                    Object object20 = chunk_19899;
                    chunk_19899 = null;
                    ++i_19901;
                    chunk_19899 = object20;
                    seq_19898 = object19;
                    continue;
                }
                Object object21 = seq_19898;
                seq_19898 = null;
                Object object22 = temp__5457__auto__19928 = ((IFn)const__1.getRawRoot()).invoke(object21);
                if (object22 == null || object22 == Boolean.FALSE) break;
                Object object23 = temp__5457__auto__19928;
                temp__5457__auto__19928 = null;
                Object seq_198982 = object23;
                Object object24 = ((IFn)const__19.getRawRoot()).invoke(seq_198982);
                if (object24 != null && object24 != Boolean.FALSE) {
                    Object c__5719__auto__19927 = ((IFn)const__20.getRawRoot()).invoke(seq_198982);
                    Object object25 = seq_198982;
                    seq_198982 = null;
                    Object object26 = c__5719__auto__19927;
                    Object object27 = c__5719__auto__19927;
                    c__5719__auto__19927 = null;
                    i_19901 = RT.intCast((long)0L);
                    count_19900 = RT.intCast((int)RT.count((Object)object27));
                    chunk_19899 = object26;
                    seq_19898 = ((IFn)const__21.getRawRoot()).invoke(object25);
                    continue;
                }
                Object id = ((IFn)const__23.getRawRoot()).invoke(seq_198982);
                Object object28 = RT.get((Object)active, (Object)id);
                if (object28 != null && object28 != Boolean.FALSE) {
                    ((IFn)const__8.getRawRoot()).invoke((Object)"Database has been restored, skipping ", id);
                    Object object29 = id;
                    id = null;
                    ((IFn)const__14.getRawRoot()).invoke(system_cluster2, object29);
                } else {
                    Object db_cluster;
                    Object db_cluster_conf;
                    ((IFn)const__8.getRawRoot()).invoke((Object)"Deleting storage for ", id);
                    Object object30 = id;
                    id = null;
                    Object object31 = db_cluster_conf = ((IFn)const__15.getRawRoot()).invoke(uri2, object30);
                    db_cluster_conf = null;
                    Object object32 = db_cluster = ((IFn)const__16.getRawRoot()).invoke(object31);
                    db_cluster = null;
                    ((IFn)const__17.getRawRoot()).invoke(system_cluster2, object32, const__8.getRawRoot());
                }
                Object object33 = seq_198982;
                seq_198982 = null;
                i_19901 = 0L;
                count_19900 = 0L;
                chunk_19899 = null;
                seq_19898 = ((IFn)const__24.getRawRoot()).invoke(object33);
            }
            Thread.sleep(100L);
            Object object34 = n = ((IFn)const__26.getRawRoot()).invoke((Object)new garbage$gc_deleted_dbs$fn__19903(), const__12, ((IFn)const__27.getRawRoot()).invoke(const__28.getRawRoot()));
            n = null;
            object = ((IFn)const__8.getRawRoot()).invoke((Object)"Deleted", object34, (Object)"catalog segments");
        } else {
            object = ((IFn)const__8.getRawRoot()).invoke((Object)"GC deleted dbs: no deleted dbs found.");
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return garbage$gc_deleted_dbs.invokeStatic(object2);
    }
}

