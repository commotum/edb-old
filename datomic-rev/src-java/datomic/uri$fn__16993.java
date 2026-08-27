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
import clojure.lang.Var;
import datomic.uri$fn__16993$fn__16996;

public final class uri$fn__16993
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__2 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__3 = RT.keyword(null, (String)"table");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__7 = RT.keyword(null, (String)"db-name");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"user"), (Object)RT.keyword(null, (String)"password"), (Object)RT.keyword(null, (String)"ssl"));
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__15 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object object2;
        Object map__16994;
        Object object3;
        Object system_root = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__2));
        ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(cluster_conf, (Object)const__3));
        Object map__169942 = cluster_conf;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke(map__169942);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__169942;
            map__169942 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__5.getRawRoot()).invoke(object5)));
        } else {
            object3 = map__169942;
            map__169942 = null;
        }
        Object object6 = map__16994 = object3;
        map__16994 = null;
        Object db_name = RT.get((Object)object6, (Object)const__7);
        Object object7 = cluster_conf;
        cluster_conf = null;
        Object params = ((IFn)const__8.getRawRoot()).invoke(object7, (Object)const__12);
        IFn iFn = (IFn)const__13.getRawRoot();
        Object object8 = system_root;
        system_root = null;
        Object object9 = db_name;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = db_name;
            db_name = null;
            object2 = ((IFn)const__13.getRawRoot()).invoke((Object)"/", object10);
        } else {
            object2 = null;
        }
        Object object11 = ((IFn)const__14.getRawRoot()).invoke(params);
        if (object11 != null && object11 != Boolean.FALSE) {
            object = null;
        } else {
            Object object12 = params;
            params = null;
            object = ((IFn)const__13.getRawRoot()).invoke((Object)"?", ((IFn)const__15.getRawRoot()).invoke((Object)"&", ((IFn)const__16.getRawRoot()).invoke((Object)new uri$fn__16993$fn__16996(), object12)));
        }
        return iFn.invoke((Object)"datomic:cass2://", object8, object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16993.invokeStatic(object2);
    }
}

