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
import datomic.kv_sql_ext$cluster_conf__GT_spec$fn__11569;

public final class kv_sql_ext$cluster_conf__GT_spec
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"sql-url");
    public static final Keyword const__4 = RT.keyword(null, (String)"data-source");
    public static final Keyword const__5 = RT.keyword(null, (String)"factory");
    public static final Var const__6 = RT.var((String)"datomic.kv-sql-ext", (String)"driver-manager-lock");
    public static final Var const__7 = RT.var((String)"datomic.kv-sql-ext", (String)"create-datasource");
    public static final Keyword const__8 = RT.keyword(null, (String)"datasource");
    public static final Keyword const__9 = RT.keyword(null, (String)"else");
    public static final Var const__10 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"invalid-sql-connection");

    public static Object invokeStatic(Object p__11567) {
        Object object;
        Object map__11568;
        Object object2;
        Object object3 = p__11567;
        p__11567 = null;
        Object map__115682 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__115682);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__115682;
            map__115682 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__115682;
            map__115682 = null;
        }
        Object cluster_conf = map__11568 = object2;
        Object sql_url2 = RT.get((Object)map__11568, (Object)const__3);
        Object data_source = RT.get((Object)map__11568, (Object)const__4);
        Object object6 = map__11568;
        map__11568 = null;
        Object factory = RT.get((Object)object6, (Object)const__5);
        Object object7 = sql_url2;
        sql_url2 = null;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8;
            Object lockee__5436__auto__11572 = const__6.getRawRoot();
            try {
                synchronized (lockee__5436__auto__11572) {
                    Object object9 = cluster_conf;
                    cluster_conf = null;
                    object8 = ((IFn)const__7.getRawRoot()).invoke(object9);
                }
            }
            finally {
                Object object10 = lockee__5436__auto__11572;
                lockee__5436__auto__11572 = null;
                // ** MonitorExit[v7] (shouldn't be in output)
            }
            {
                object = object8;
            }
        } else {
            Object object11 = data_source;
            if (object11 != null && object11 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__8;
                Object object12 = data_source;
                data_source = null;
                objectArray[1] = object12;
                object = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                Object object13 = factory;
                if (object13 != null && object13 != Boolean.FALSE) {
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__5;
                    Object object14 = factory;
                    factory = null;
                    objectArray[1] = new kv_sql_ext$cluster_conf__GT_spec$fn__11569(object14);
                    object = RT.mapUniqueKeys((Object[])objectArray);
                } else {
                    Keyword keyword = const__9;
                    object = keyword != null && keyword != Boolean.FALSE ? ((IFn)const__10.getRawRoot()).invoke((Object)const__11, (Object)"Must supply jdbc url in uri, or DataSource or Callable<Connection> in protocolObject arg to Peer.connect") : null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql_ext$cluster_conf__GT_spec.invokeStatic(object2);
    }
}

