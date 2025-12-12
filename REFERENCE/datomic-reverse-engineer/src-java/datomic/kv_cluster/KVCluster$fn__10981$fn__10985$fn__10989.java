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
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster$fn__10981$fn__10985$fn__10989$fn__10991;

public final class KVCluster$fn__10981$fn__10985$fn__10989
extends AFunction {
    Object kvs;
    Object retrying_read;
    Object tail;
    public static final Keyword const__0 = RT.keyword(null, (String)"linear");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"v");
    public static final Keyword const__5 = RT.keyword(null, (String)"prev");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__7 = RT.keyword(null, (String)"tail");
    public static final Keyword const__8 = RT.keyword(null, (String)"t");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"cons");

    public KVCluster$fn__10981$fn__10985$fn__10989(Object object, Object object2, Object object3) {
        this.kvs = object;
        this.retrying_read = object2;
        this.tail = object3;
    }

    public Object invoke() {
        Object ret;
        Object tail = this.tail;
        Object ret2 = null;
        while (true) {
            Object map__10990;
            Object object;
            Object map__109902 = ((IFn)this.retrying_read).invoke((Object)const__0, (Object)new KVCluster$fn__10981$fn__10985$fn__10989$fn__10991(this.kvs, tail));
            Object object2 = ((IFn)const__1.getRawRoot()).invoke(map__109902);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = map__109902;
                map__109902 = null;
                object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object3)));
            } else {
                object = map__109902;
                map__109902 = null;
            }
            Object t = map__10990 = object;
            Object v = RT.get((Object)map__10990, (Object)const__4);
            Object object4 = map__10990;
            map__10990 = null;
            Object prev = RT.get((Object)object4, (Object)const__5);
            Object object5 = v;
            if (object5 == null || object5 == Boolean.FALSE) {
                Object[] objectArray = new Object[8];
                objectArray[0] = const__7;
                Object object6 = tail;
                tail = null;
                objectArray[1] = object6;
                objectArray[2] = const__8;
                Object object7 = t;
                t = null;
                objectArray[3] = object7;
                objectArray[4] = const__4;
                objectArray[5] = v;
                objectArray[6] = const__5;
                objectArray[7] = prev;
                throw (Throwable)((IFn)const__6.getRawRoot()).invoke((Object)"Key missing in storage", (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            Object object8 = v;
            v = null;
            Object object9 = ret2;
            ret2 = null;
            ret = ((IFn)const__9.getRawRoot()).invoke(object8, object9);
            Object object10 = prev;
            if (object10 == null || object10 == Boolean.FALSE) break;
            Object object11 = prev;
            prev = null;
            Object object12 = ret;
            ret = null;
            ret2 = object12;
            tail = object11;
        }
        Object object = ret;
        ret = null;
        return object;
    }
}

