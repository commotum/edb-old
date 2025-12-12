/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.tomcat.jdbc.pool.DataSource
 *  org.apache.tomcat.jdbc.pool.DataSourceProxy
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_sql_ext$fn__11557$fn__11560;
import datomic.kv_sql_ext$fn__11557$fn__11562;
import datomic.kv_sql_ext$fn__11557$fn__11564;
import java.sql.DriverManager;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;

public final class kv_sql_ext$fn__11557
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"sql-url");
    public static final Keyword const__4 = RT.keyword(null, (String)"sql-user");
    public static final Keyword const__5 = RT.keyword(null, (String)"sql-password");
    public static final Keyword const__6 = RT.keyword(null, (String)"sql-driver-class");
    public static final Keyword const__7 = RT.keyword(null, (String)"sql-driver-params");
    public static final Keyword const__8 = RT.keyword(null, (String)"sql-initial-size");
    public static final Object const__9 = 2L;
    public static final Var const__10 = RT.var((String)"datomic.kv-sql-ext", (String)"validation-query");
    public static final Keyword const__11 = RT.keyword(null, (String)"datasource");
    public static final Var const__12 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__13 = RT.var((String)"datomic.kv-sql-ext", (String)"try-validation-query");

    public static Object invokeStatic(Object p__11556) {
        Object object;
        Object object2;
        Object object3 = p__11556;
        p__11556 = null;
        Object map__11558 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11558);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11558;
            map__11558 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11558;
            map__11558 = null;
        }
        Object map__115582 = object2;
        Object sql_url2 = RT.get((Object)map__115582, (Object)const__3);
        Object sql_user = RT.get((Object)map__115582, (Object)const__4);
        Object sql_password = RT.get((Object)map__115582, (Object)const__5);
        Object sql_driver_class = RT.get((Object)map__115582, (Object)const__6);
        Object sql_driver_params = RT.get((Object)map__115582, (Object)const__7);
        Object object6 = map__115582;
        map__115582 = null;
        Object sql_initial_size = RT.get((Object)object6, (Object)const__8, (Object)const__9);
        Object vq = ((IFn)const__10.getRawRoot()).invoke(sql_url2);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__11;
        DataSource G__11559 = new DataSource();
        ((DataSourceProxy)G__11559).setUrl((String)sql_url2);
        Object object7 = vq;
        vq = null;
        ((DataSourceProxy)G__11559).setValidationQuery((String)object7);
        ((DataSourceProxy)G__11559).setValidationInterval(RT.longCast((Object)((Number)((IFn)const__12.getRawRoot()).invoke((Object)"datomic.heartbeatIntervalMsec"))));
        ((DataSourceProxy)G__11559).setTestOnBorrow(Boolean.TRUE.booleanValue());
        Object object8 = sql_initial_size;
        sql_initial_size = null;
        ((DataSourceProxy)G__11559).setInitialSize(RT.intCast((Object)((Number)object8)));
        DataSourceProxy dataSourceProxy = (DataSourceProxy)G__11559;
        Object object9 = sql_driver_class;
        if (object9 != null && object9 != Boolean.FALSE) {
            object = sql_driver_class;
            sql_driver_class = null;
        } else {
            object = DriverManager.getDriver((String)sql_url2).getClass().getName();
        }
        dataSourceProxy.setDriverClassName((String)object);
        Object object10 = sql_user;
        sql_user = null;
        ((IFn)new kv_sql_ext$fn__11557$fn__11560(object10)).invoke((Object)G__11559);
        Object object11 = sql_password;
        sql_password = null;
        ((IFn)new kv_sql_ext$fn__11557$fn__11562(object11)).invoke((Object)G__11559);
        Object object12 = sql_driver_params;
        sql_driver_params = null;
        ((IFn)new kv_sql_ext$fn__11557$fn__11564(object12)).invoke((Object)G__11559);
        DataSource dataSource = G__11559;
        G__11559 = null;
        objectArray[1] = dataSource;
        IPersistentMap spec = RT.mapUniqueKeys((Object[])objectArray);
        Object object13 = sql_url2;
        sql_url2 = null;
        ((IFn)const__13.getRawRoot()).invoke(object13, (Object)spec);
        IPersistentMap iPersistentMap = spec;
        spec = null;
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql_ext$fn__11557.invokeStatic(object2);
    }
}

