/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOOL
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Db;

public final class db$db
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"root-id");
    public static final Keyword const__4 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__5 = RT.keyword(null, (String)"index");
    public static final Keyword const__6 = RT.keyword(null, (String)"history");
    public static final Keyword const__7 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__8 = RT.keyword(null, (String)"nextT");
    public static final Keyword const__9 = RT.keyword(null, (String)"schema-level");
    public static final Keyword const__10 = RT.keyword(null, (String)"birth-level");
    public static final Keyword const__11 = RT.keyword(null, (String)"rev");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"load-builtins");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"find-last-tx");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"mem-index-set");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"memlog");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"finish-init");
    public static final Var const__18 = RT.var((String)"datomic.db", (String)"run-hooks");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");

    public static Object invokeStatic(Object id, Object p__13625) {
        Db db2;
        Object object;
        Object object2;
        Object object3 = p__13625;
        p__13625 = null;
        Object map__13626 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__13626);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__13626;
            map__13626 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__13626;
            map__13626 = null;
        }
        Object map__136262 = object2;
        Object root_id2 = RT.get((Object)map__136262, (Object)const__3);
        Object mid_index = RT.get((Object)map__136262, (Object)const__4);
        Object index2 = RT.get((Object)map__136262, (Object)const__5);
        Object history2 = RT.get((Object)map__136262, (Object)const__6);
        Object basisT = RT.get((Object)map__136262, (Object)const__7);
        Object nextT = RT.get((Object)map__136262, (Object)const__8);
        Object schema_level = RT.get((Object)map__136262, (Object)const__9);
        Object birth_level = RT.get((Object)map__136262, (Object)const__10);
        Object object6 = map__136262;
        map__136262 = null;
        Object rev = RT.get((Object)object6, (Object)const__11);
        ((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot());
        Object object7 = basisT;
        if (object7 != null && object7 != Boolean.FALSE) {
            object = basisT;
            basisT = null;
        } else {
            object = Numbers.num((long)((IFn.LOOL)const__14.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)nextT)), index2, mid_index));
        }
        Object basisT2 = object;
        Object object8 = id;
        id = null;
        Object object9 = mid_index;
        mid_index = null;
        Object object10 = history2;
        history2 = null;
        long l = RT.uncheckedLongCast((Object)((Number)basisT2));
        Object object11 = nextT;
        nextT = null;
        Object object12 = basisT2;
        basisT2 = null;
        Object object13 = root_id2;
        root_id2 = null;
        Object object14 = rev;
        rev = null;
        Db db3 = db2 = new Db(object8, const__15.getRawRoot(), null, object9, index2, object10, ((IFn)const__16.getRawRoot()).invoke(), l, RT.uncheckedLongCast((Object)((Number)object11)), RT.uncheckedLongCast((Object)((Number)object12)), null, PersistentVector.EMPTY, PersistentArrayMap.EMPTY, PersistentArrayMap.EMPTY, object13, object14, null, null, null, null);
        db2 = null;
        Object object15 = schema_level;
        schema_level = null;
        Object object16 = birth_level;
        birth_level = null;
        Object object17 = index2;
        index2 = null;
        return ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)db3, (Object)const__9, object15, (Object)const__10, object16), object17));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$db.invokeStatic(object3, object4);
    }
}

