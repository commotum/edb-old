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
import java.nio.ByteBuffer;

public final class backup$restore_roots
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"index-root-id");
    public static final Keyword const__4 = RT.keyword(null, (String)"log-root-id");
    public static final Keyword const__5 = RT.keyword(null, (String)"log-tail");
    public static final Keyword const__7 = RT.keyword(null, (String)"ok");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__9 = RT.var((String)"datomic.cluster", (String)"reset-ref");
    public static final Var const__10 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
    public static final Var const__11 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__12 = RT.keyword((String)"backup", (String)"roots-failed");
    public static final Keyword const__13 = RT.keyword(null, (String)"conflict");
    public static final Var const__14 = RT.var((String)"datomic.cluster", (String)"reset-pod");
    public static final Var const__15 = RT.var((String)"datomic.log", (String)"tail-pod-key");
    public static final Keyword const__16 = RT.keyword((String)"d", (String)"r");
    public static final Keyword const__17 = RT.keyword((String)"d", (String)"v");
    public static final Var const__18 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__19 = RT.keyword((String)"d", (String)"l");
    public static final Object const__20 = 3L;
    public static final Keyword const__21 = RT.keyword(null, (String)"succeeded");

    public static Object invokeStatic(Object job, Object cluster2) {
        block4: {
            Object object;
            Object object2 = job;
            job = null;
            Object map__20227 = object2;
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20227);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__20227;
                map__20227 = null;
                object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
            } else {
                object = map__20227;
                map__20227 = null;
            }
            Object map__202272 = object;
            Object index_root_id = RT.get((Object)map__202272, (Object)const__3);
            Object log_root_id = RT.get((Object)map__202272, (Object)const__4);
            Object object5 = map__202272;
            map__202272 = null;
            Object log_tail = RT.get((Object)object5, (Object)const__5);
            Object object6 = index_root_id;
            index_root_id = null;
            if (Util.equiv((Object)const__7, (Object)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(cluster2, ((IFn)const__10.getRawRoot()).invoke(cluster2), object6)))) {
            } else {
                ((IFn)const__11.getRawRoot()).invoke((Object)const__12, (Object)"Unable to restore index root");
            }
            Object object7 = cluster2;
            Object object8 = cluster2;
            cluster2 = null;
            Object object9 = log_tail;
            log_tail = null;
            Object[] objectArray = new Object[6];
            objectArray[0] = const__16;
            Object object10 = log_root_id;
            log_root_id = null;
            objectArray[1] = object10;
            objectArray[2] = const__17;
            objectArray[3] = ((IFn)const__18.getRawRoot()).invoke((Object)"datomic.versionUnique");
            objectArray[4] = const__19;
            objectArray[5] = const__20;
            if (!Util.equiv((Object)const__13, (Object)((IFn)const__8.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object7, ((IFn)const__15.getRawRoot()).invoke(object8), (Object)ByteBuffer.wrap((byte[])object9), (Object)RT.mapUniqueKeys((Object[])objectArray))))) break block4;
            ((IFn)const__11.getRawRoot()).invoke((Object)const__12, (Object)"Unable to restore log tail pod");
        }
        return const__21;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$restore_roots.invokeStatic(object3, object4);
    }
}

