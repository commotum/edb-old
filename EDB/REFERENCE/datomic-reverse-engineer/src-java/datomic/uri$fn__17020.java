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

public final class uri$fn__17020
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__5 = RT.keyword(null, (String)"password");
    public static final Keyword const__6 = RT.keyword(null, (String)"db-name");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object object2;
        Object object3;
        Object object4 = cluster_conf;
        cluster_conf = null;
        Object map__17021 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__17021);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__17021;
            map__17021 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__17021;
            map__17021 = null;
        }
        Object map__170212 = object3;
        Object host = RT.get((Object)map__170212, (Object)const__3);
        Object bucket = RT.get((Object)map__170212, (Object)const__4);
        Object password = RT.get((Object)map__170212, (Object)const__5);
        Object object7 = map__170212;
        map__170212 = null;
        Object db_name = RT.get((Object)object7, (Object)const__6);
        IFn iFn = (IFn)const__7.getRawRoot();
        Object object8 = host;
        host = null;
        Object object9 = bucket;
        bucket = null;
        Object object10 = db_name;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = db_name;
            db_name = null;
            object2 = ((IFn)const__7.getRawRoot()).invoke((Object)"/", object11);
        } else {
            object2 = null;
        }
        Object object12 = password;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object object13 = password;
            password = null;
            object = ((IFn)const__7.getRawRoot()).invoke((Object)"?password=", object13);
        } else {
            object = null;
        }
        return iFn.invoke((Object)"datomic:couchbase://", object8, (Object)"/", object9, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17020.invokeStatic(object2);
    }
}

