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
import java.sql.SQLException;
import java.util.Arrays;

public final class h2$try_connect
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"data-dir");
    public static final Keyword const__4 = RT.keyword(null, (String)"username");
    public static final Keyword const__5 = RT.keyword(null, (String)"password");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"data-dir"), Symbol.intern(null, (String)"username"), Symbol.intern(null, (String)"password")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__9 = RT.var((String)"datomic.sql", (String)"connect");
    public static final Var const__10 = RT.var((String)"datomic.h2", (String)"create-sql-spec");
    public static final Keyword const__11 = RT.keyword(null, (String)"sql-url");
    public static final Var const__12 = RT.var((String)"datomic.h2", (String)"sql-url");

    public static Object invokeStatic(Object p__11601) {
        Object object;
        Object object2;
        Object and__5236__auto__11605;
        Object object3;
        Object object4 = p__11601;
        p__11601 = null;
        Object map__11602 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__11602);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__11602;
            map__11602 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__11602;
            map__11602 = null;
        }
        Object map__116022 = object3;
        Object data_dir = RT.get((Object)map__116022, (Object)const__3);
        Object username = RT.get((Object)map__116022, (Object)const__4);
        Object object7 = map__116022;
        map__116022 = null;
        Object password = RT.get((Object)object7, (Object)const__5);
        Object object8 = and__5236__auto__11605 = data_dir;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object and__5236__auto__11604;
            Object object9 = and__5236__auto__11604 = username;
            if (object9 != null && object9 != Boolean.FALSE) {
                object2 = password;
            } else {
                object2 = and__5236__auto__11604;
                and__5236__auto__11604 = null;
            }
        } else {
            object2 = and__5236__auto__11605;
            and__5236__auto__11605 = null;
        }
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__6.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__7.getRawRoot()).invoke(const__8))));
        }
        try {
            Object[] objectArray = new Object[6];
            objectArray[0] = const__11;
            Object object10 = data_dir;
            data_dir = null;
            objectArray[1] = ((IFn)const__12.getRawRoot()).invoke(object10);
            objectArray[2] = const__4;
            Object object11 = username;
            username = null;
            objectArray[3] = object11;
            objectArray[4] = const__5;
            Object object12 = password;
            password = null;
            objectArray[5] = object12;
            object = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
        }
        catch (SQLException e2) {
            if ((long)e2.getErrorCode() != 28000L) {
                Object e2 = null;
                throw (Throwable)e2;
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$try_connect.invokeStatic(object2);
    }
}

