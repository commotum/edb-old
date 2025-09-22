/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class kv_sql_ext$try_validation_query
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.kv-sql-ext", (String)"validation-query");
    public static final Var const__1 = RT.var((String)"datomic.sql", (String)"connect");
    public static final Var const__2 = RT.var((String)"datomic.monitor", (String)"alarm");
    public static final Keyword const__3 = RT.keyword(null, (String)"SQLValidationQueryFailed");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__5 = RT.keyword(null, (String)"event");
    public static final Keyword const__6 = RT.keyword((String)"sql", (String)"validation-query-failed");
    public static final Keyword const__7 = RT.keyword(null, (String)"query");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public static Object invokeStatic(Object sql_url2, Object spec) {
        Object var10_14;
        Object object = sql_url2;
        sql_url2 = null;
        Object q2 = ((IFn)const__0.getRawRoot()).invoke(object);
        try {
            Object var8_10;
            Object object2 = spec;
            spec = null;
            Object conn = ((IFn)const__1.getRawRoot()).invoke(object2);
            try {
                Object var6_8;
                PreparedStatement stmt = ((Connection)conn).prepareStatement((String)q2);
                try {
                    Object v2;
                    if (stmt.executeQuery().next()) {
                        v2 = null;
                    } else {
                        ((IFn)const__2.getRawRoot()).invoke((Object)const__3);
                        Logger logger = LoggerFactory.getLogger((String)"datomic.kv-sql-ext");
                        if (logger.isWarnEnabled()) {
                            Logger logger2 = logger;
                            logger = null;
                            logger2.warn((String)((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__5, const__6, const__7, q2})));
                        }
                        v2 = null;
                    }
                    var6_8 = v2;
                }
                finally {
                    PreparedStatement preparedStatement = stmt;
                    stmt = null;
                    ((Statement)preparedStatement).close();
                }
                var8_10 = var6_8;
            }
            finally {
                Object object3 = conn;
                conn = null;
                ((Connection)object3).close();
            }
            var10_14 = var8_10;
        }
        catch (Throwable t2) {
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3);
            Logger logger = LoggerFactory.getLogger((String)"datomic.kv-sql-ext");
            Object t2 = null;
            Throwable ex = t2;
            if (logger.isWarnEnabled()) {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__5;
                objectArray[1] = const__6;
                objectArray[2] = const__7;
                Object object4 = q2;
                q2 = null;
                objectArray[3] = object4;
                logger.warn((String)((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)), ex);
                Logger logger3 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__8.getRawRoot()).invoke((Object)logger3, (Object)throwable);
            }
            var10_14 = null;
        }
        return var10_14;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return kv_sql_ext$try_validation_query.invokeStatic(object3, object4);
    }
}

