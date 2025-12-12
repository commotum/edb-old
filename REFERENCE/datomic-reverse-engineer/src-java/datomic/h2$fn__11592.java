/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  org.apache.tomcat.jdbc.pool.DataSource
 *  org.apache.tomcat.jdbc.pool.DataSourceProxy
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;

public final class h2$fn__11592
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"sql-url");
    public static final Keyword const__4 = RT.keyword(null, (String)"username");
    public static final Keyword const__5 = RT.keyword(null, (String)"password");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"sql-url"), Symbol.intern(null, (String)"username"), Symbol.intern(null, (String)"password")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 14}));
    public static final Keyword const__9 = RT.keyword(null, (String)"datasource");

    public static Object invokeStatic(Object p__11591) {
        Object object;
        Object and__5236__auto__11597;
        Object object2;
        Object object3 = p__11591;
        p__11591 = null;
        Object map__11593 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11593);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11593;
            map__11593 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11593;
            map__11593 = null;
        }
        Object map__115932 = object2;
        Object sql_url2 = RT.get((Object)map__115932, (Object)const__3);
        Object username = RT.get((Object)map__115932, (Object)const__4);
        Object object6 = map__115932;
        map__115932 = null;
        Object password = RT.get((Object)object6, (Object)const__5);
        Object object7 = and__5236__auto__11597 = sql_url2;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object and__5236__auto__11596;
            Object object8 = and__5236__auto__11596 = username;
            if (object8 != null && object8 != Boolean.FALSE) {
                object = password;
            } else {
                object = and__5236__auto__11596;
                and__5236__auto__11596 = null;
            }
        } else {
            object = and__5236__auto__11597;
            and__5236__auto__11597 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__6.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__7.getRawRoot()).invoke(const__8))));
        }
        Object[] objectArray = new Object[2];
        objectArray[0] = const__9;
        DataSource G__11594 = new DataSource();
        Object object9 = sql_url2;
        sql_url2 = null;
        ((DataSourceProxy)G__11594).setUrl((String)object9);
        Object object10 = username;
        username = null;
        ((DataSourceProxy)G__11594).setUsername((String)object10);
        Object object11 = password;
        password = null;
        ((DataSourceProxy)G__11594).setPassword((String)object11);
        ((DataSourceProxy)G__11594).setDriverClassName("org.h2.Driver");
        ((DataSourceProxy)G__11594).setValidationQuery("SELECT 1");
        ((DataSourceProxy)G__11594).setTestWhileIdle(Boolean.TRUE.booleanValue());
        ((DataSourceProxy)G__11594).setInitialSize(RT.intCast((long)2L));
        DataSource dataSource = G__11594;
        G__11594 = null;
        objectArray[1] = dataSource;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$fn__11592.invokeStatic(object2);
    }
}

