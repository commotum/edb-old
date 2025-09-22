/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$resolve_dbid
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"part");
    public static final Keyword const__4 = RT.keyword(null, (String)"idx");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"partbits");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"make-tempid");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"make-eid");

    public static Object invokeStatic(Object db2, Object x) {
        Number number;
        Object object;
        Object object2 = x;
        x = null;
        Object map__12605 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__12605);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__12605;
            map__12605 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__12605;
            map__12605 = null;
        }
        Object map__126052 = object;
        Object part2 = RT.get((Object)map__126052, (Object)const__3);
        Object object5 = map__126052;
        map__126052 = null;
        Object idx = RT.get((Object)object5, (Object)const__4);
        Object object6 = db2;
        db2 = null;
        Object object7 = part2;
        part2 = null;
        Object part3 = ((IFn)const__5.getRawRoot()).invoke(object6, object7);
        if (Numbers.isNeg((Object)idx)) {
            Object object8 = part3;
            part3 = null;
            Object object9 = idx;
            idx = null;
            number = Numbers.num((long)((IFn.LLL)const__7.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object8)), RT.uncheckedLongCast((Object)((Number)object9))));
        } else {
            Object object10 = part3;
            part3 = null;
            Object object11 = idx;
            idx = null;
            number = Numbers.num((long)((IFn.LLL)const__8.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object10)), RT.uncheckedLongCast((Object)((Number)object11))));
        }
        return number;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$resolve_dbid.invokeStatic(object3, object4);
    }
}

