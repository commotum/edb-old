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
import java.util.Arrays;

public final class h2$remote_jdbc_spec
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"h2-port");
    public static final Keyword const__5 = RT.keyword(null, (String)"password");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"host"), Symbol.intern(null, (String)"h2-port"), Symbol.intern(null, (String)"password")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__9 = RT.var((String)"datomic.h2", (String)"create-sql-spec");
    public static final Keyword const__10 = RT.keyword(null, (String)"sql-url");
    public static final Keyword const__11 = RT.keyword(null, (String)"username");

    public static Object invokeStatic(Object p__11651) {
        Object object;
        Object and__5236__auto__11655;
        Object object2;
        Object object3 = p__11651;
        p__11651 = null;
        Object map__11652 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11652);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11652;
            map__11652 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11652;
            map__11652 = null;
        }
        Object map__116522 = object2;
        Object host = RT.get((Object)map__116522, (Object)const__3);
        Object h2_port = RT.get((Object)map__116522, (Object)const__4);
        Object object6 = map__116522;
        map__116522 = null;
        Object password = RT.get((Object)object6, (Object)const__5);
        Object object7 = and__5236__auto__11655 = host;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object and__5236__auto__11654;
            Object object8 = and__5236__auto__11654 = h2_port;
            if (object8 != null && object8 != Boolean.FALSE) {
                object = password;
            } else {
                object = and__5236__auto__11654;
                and__5236__auto__11654 = null;
            }
        } else {
            object = and__5236__auto__11655;
            and__5236__auto__11655 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__6.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__7.getRawRoot()).invoke(const__8))));
        }
        Object[] objectArray = new Object[6];
        objectArray[0] = const__10;
        Object object9 = host;
        host = null;
        Object object10 = h2_port;
        h2_port = null;
        objectArray[1] = ((IFn)const__6.getRawRoot()).invoke((Object)"jdbc:h2:tcp://", object9, (Object)":", object10, (Object)"/datomic");
        objectArray[2] = const__11;
        objectArray[3] = "datomic";
        objectArray[4] = const__5;
        Object object11 = password;
        password = null;
        objectArray[5] = object11;
        return ((IFn)const__9.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$remote_jdbc_spec.invokeStatic(object2);
    }
}

