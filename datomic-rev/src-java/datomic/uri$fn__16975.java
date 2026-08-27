/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.uri$fn__16975$fn__16978;

public final class uri$fn__16975
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__4 = RT.keyword(null, (String)"system");
    public static final Keyword const__5 = RT.keyword(null, (String)"aws-dynamodb-table");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__9 = RT.keyword(null, (String)"db-name");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"skip-efs"));
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__14 = RT.keyword(null, (String)"aws-region");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__16 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object object2;
        Object map__16976;
        Object object3;
        Object object4;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        if (Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__4), (Object)"_default")) {
            object4 = ((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__5);
        } else {
            object4 = system_root;
            system_root = null;
        }
        Object root_or_table = object4;
        Object map__169762 = cluster_conf;
        Object object5 = ((IFn)const__6.getRawRoot()).invoke(map__169762);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__169762;
            map__169762 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__7.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__169762;
            map__169762 = null;
        }
        Object object7 = map__16976 = object3;
        map__16976 = null;
        Object db_name = RT.get((Object)object7, (Object)const__9);
        Object params = ((IFn)const__10.getRawRoot()).invoke(cluster_conf, (Object)const__12);
        IFn iFn = (IFn)const__13.getRawRoot();
        Object object8 = cluster_conf;
        cluster_conf = null;
        Object object9 = ((IFn)const__1.getRawRoot()).invoke(object8, (Object)const__14);
        Object object10 = root_or_table;
        root_or_table = null;
        Object object11 = db_name;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object object12 = db_name;
            db_name = null;
            object2 = ((IFn)const__13.getRawRoot()).invoke((Object)"/", object12);
        } else {
            object2 = null;
        }
        Object object13 = ((IFn)const__15.getRawRoot()).invoke(params);
        if (object13 != null && object13 != Boolean.FALSE) {
            object = null;
        } else {
            Object object14 = params;
            params = null;
            object = ((IFn)const__13.getRawRoot()).invoke((Object)"?", ((IFn)const__16.getRawRoot()).invoke((Object)"&", ((IFn)const__17.getRawRoot()).invoke((Object)new uri$fn__16975$fn__16978(), object14)));
        }
        return iFn.invoke((Object)"datomic:ddb+s3://", object9, (Object)"/", object10, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16975.invokeStatic(object2);
    }
}

