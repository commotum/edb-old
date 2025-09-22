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
package datomic.kv_sql;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KVSql$fn__11577
extends AFunction {
    Object id;
    Object spec;
    Object val_map;
    public static final Var const__0 = RT.var((String)"datomic.sql", (String)"insert");
    public static final Var const__1 = RT.var((String)"datomic.kv-sql", (String)"constraint-violation?");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__3 = RT.keyword(null, (String)"event");
    public static final Keyword const__4 = RT.keyword((String)"kv-sql", (String)"put-failed");
    public static final Keyword const__5 = RT.keyword(null, (String)"id");
    public static final Keyword const__6 = RT.keyword(null, (String)"desc");

    public KVSql$fn__11577(Object object, Object object2, Object object3) {
        this.id = object;
        this.spec = object2;
        this.val_map = object3;
    }

    public Object invoke() {
        Boolean bl;
        try {
            ((IFn)const__0.getRawRoot()).invoke(this.spec, this.val_map);
            bl = Boolean.TRUE;
        }
        catch (SQLException ex2) {
            Object object = ((IFn)const__1.getRawRoot()).invoke((Object)ex2);
            if (object != null && object != Boolean.FALSE) {
                Logger logger = LoggerFactory.getLogger((String)"datomic.kv-sql");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    Object[] objectArray = new Object[6];
                    objectArray[0] = const__3;
                    objectArray[1] = const__4;
                    objectArray[2] = const__5;
                    objectArray[3] = this.id;
                    objectArray[4] = const__6;
                    Object ex2 = null;
                    objectArray[5] = ((Throwable)ex2).getMessage();
                    logger2.info((String)((IFn)const__2.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                }
            } else {
                Object ex2 = null;
                throw (Throwable)ex2;
            }
            bl = Boolean.FALSE;
        }
        return bl;
    }
}

