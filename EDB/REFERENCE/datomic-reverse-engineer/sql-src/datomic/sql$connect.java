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
import javax.sql.DataSource;

public final class sql$connect
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"datasource");
    public static final Keyword const__4 = RT.keyword(null, (String)"factory");
    public static final Keyword const__5 = RT.keyword(null, (String)"else");
    public static final Var const__6 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"invalid-sql-connection");

    public static Object invokeStatic(Object p__11481) {
        Object object;
        Object object2;
        Object object3 = p__11481;
        p__11481 = null;
        Object map__11482 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11482);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11482;
            map__11482 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11482;
            map__11482 = null;
        }
        Object map__114822 = object2;
        Object datasource = RT.get((Object)map__114822, (Object)const__3);
        Object object6 = map__114822;
        map__114822 = null;
        Object factory = RT.get((Object)object6, (Object)const__4);
        Object object7 = datasource;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = datasource;
            datasource = null;
            object = ((DataSource)object8).getConnection();
        } else {
            Object object9 = factory;
            if (object9 != null && object9 != Boolean.FALSE) {
                Object object10 = factory;
                factory = null;
                object = ((IFn)object10).invoke();
            } else {
                Keyword keyword = const__5;
                object = keyword != null && keyword != Boolean.FALSE ? ((IFn)const__6.getRawRoot()).invoke((Object)const__7, (Object)"Must supply DataSource or Callable<Connection>") : null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return sql$connect.invokeStatic(object2);
    }
}
