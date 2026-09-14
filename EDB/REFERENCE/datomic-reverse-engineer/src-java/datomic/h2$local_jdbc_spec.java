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
 *  clojure.lang.Symbol
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
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class h2$local_jdbc_spec
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"data-dir");
    public static final Keyword const__4 = RT.keyword(null, (String)"storage-datomic-password");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__7 = (AFn)Symbol.intern(null, (String)"data-dir");
    public static final Var const__8 = RT.var((String)"datomic.h2", (String)"create-sql-spec");
    public static final Keyword const__9 = RT.keyword(null, (String)"sql-url");
    public static final Keyword const__10 = RT.keyword(null, (String)"username");
    public static final Keyword const__11 = RT.keyword(null, (String)"password");

    public static Object invokeStatic(Object p__11647) {
        Object object;
        Object or__5238__auto__11650;
        Object object2;
        Object object3 = p__11647;
        p__11647 = null;
        Object map__11648 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11648);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11648;
            map__11648 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11648;
            map__11648 = null;
        }
        Object map__116482 = object2;
        Object data_dir = RT.get((Object)map__116482, (Object)const__3);
        Object object6 = map__116482;
        map__116482 = null;
        Object storage_datomic_password = RT.get((Object)object6, (Object)const__4);
        Object object7 = data_dir;
        if (object7 == null || object7 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__5.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__6.getRawRoot()).invoke((Object)const__7))));
        }
        IFn iFn = (IFn)const__8.getRawRoot();
        Object[] objectArray = new Object[6];
        objectArray[0] = const__9;
        Object object8 = data_dir;
        data_dir = null;
        objectArray[1] = ((IFn)const__5.getRawRoot()).invoke((Object)"jdbc:h2:", object8, (Object)"/datomic");
        objectArray[2] = const__10;
        objectArray[3] = "datomic";
        objectArray[4] = const__11;
        Object object9 = storage_datomic_password;
        storage_datomic_password = null;
        Object object10 = or__5238__auto__11650 = object9;
        if (object10 != null && object10 != Boolean.FALSE) {
            object = or__5238__auto__11650;
            or__5238__auto__11650 = null;
        } else {
            object = "datomic";
        }
        objectArray[5] = object;
        return iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$local_jdbc_spec.invokeStatic(object2);
    }
}

