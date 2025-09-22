/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class coordination_ext$fn__16747
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"port");
    public static final Var const__5 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    public static final Var const__6 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"datomic.kv-hotrod", (String)"kv-infinispan");

    public static Object invokeStatic(Object cluster_conf) {
        IPersistentMap endpoint;
        Object object;
        Object map__16748 = cluster_conf;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__16748);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__16748;
            map__16748 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object3)));
        } else {
            object = map__16748;
            map__16748 = null;
        }
        Object map__167482 = object;
        Object host = RT.get((Object)map__167482, (Object)const__3);
        Object object4 = map__167482;
        map__167482 = null;
        Object port = RT.get((Object)object4, (Object)const__4);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__3;
        Object object5 = host;
        host = null;
        objectArray[1] = object5;
        objectArray[2] = const__4;
        Object object6 = port;
        port = null;
        objectArray[3] = object6;
        IPersistentMap iPersistentMap = endpoint = RT.mapUniqueKeys((Object[])objectArray);
        endpoint = null;
        Object object7 = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__7, (Object)iPersistentMap), object7);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination_ext$fn__16747.invokeStatic(object2);
    }
}

