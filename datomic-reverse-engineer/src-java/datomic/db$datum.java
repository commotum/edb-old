/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.db.Datum;

public final class db$datum
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"asserting");
    public static final Keyword const__4 = RT.keyword(null, (String)"e");
    public static final Keyword const__5 = RT.keyword(null, (String)"a");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");
    public static final Keyword const__7 = RT.keyword(null, (String)"t");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Object const__9 = -1L;
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"retracting-datum");

    public static Object invokeStatic(Object db2, ISeq p__12635) {
        Object object;
        Object object2;
        Object or__5238__auto__12639;
        Object object3;
        Object or__5238__auto__12638;
        ISeq iSeq;
        ISeq iSeq2 = p__12635;
        p__12635 = null;
        ISeq map__12636 = iSeq2;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke((Object)map__12636);
        if (object4 != null && object4 != Boolean.FALSE) {
            ISeq iSeq3 = map__12636;
            map__12636 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12636;
            map__12636 = null;
        }
        ISeq map__126362 = iSeq;
        Object asserting = RT.get((Object)map__126362, (Object)const__3, (Object)Boolean.TRUE);
        Object e = RT.get((Object)map__126362, (Object)const__4);
        Object a = RT.get((Object)map__126362, (Object)const__5);
        Object v = RT.get((Object)map__126362, (Object)const__6);
        ISeq iSeq4 = map__126362;
        map__126362 = null;
        Object t = RT.get((Object)iSeq4, (Object)const__7);
        IFn iFn = (IFn)const__8.getRawRoot();
        Object object5 = e;
        e = null;
        Object object6 = or__5238__auto__12638 = object5;
        if (object6 != null && object6 != Boolean.FALSE) {
            object3 = or__5238__auto__12638;
            or__5238__auto__12638 = null;
        } else {
            object3 = Numbers.num((long)Long.MIN_VALUE);
        }
        Object e2 = iFn.invoke(db2, object3);
        Object object7 = db2;
        db2 = null;
        Object object8 = a;
        a = null;
        Object object9 = or__5238__auto__12639 = ((IFn)const__8.getRawRoot()).invoke(object7, object8);
        if (object9 != null && object9 != Boolean.FALSE) {
            object2 = or__5238__auto__12639;
            or__5238__auto__12639 = null;
        } else {
            object2 = const__9;
        }
        Object a2 = object2;
        Object object10 = t;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = asserting;
            asserting = null;
            if (object11 != null && object11 != Boolean.FALSE) {
                Object object12 = e2;
                e2 = null;
                Object object13 = a2;
                a2 = null;
                Object object14 = v;
                v = null;
                Object object15 = t;
                t = null;
                object = ((IFn.LLOLO)const__10.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object12)), RT.uncheckedLongCast((Object)((Number)object13)), object14, RT.uncheckedLongCast((Object)((Number)object15)));
            } else {
                Object object16 = e2;
                e2 = null;
                Object object17 = a2;
                a2 = null;
                Object object18 = v;
                v = null;
                Object object19 = t;
                t = null;
                object = ((IFn.LLOLO)const__11.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object16)), RT.uncheckedLongCast((Object)((Number)object17)), object18, RT.uncheckedLongCast((Object)((Number)object19)));
            }
        } else {
            Object object20 = e2;
            e2 = null;
            Object object21 = a2;
            a2 = null;
            v = null;
            Object object22 = asserting;
            asserting = null;
            object = new Datum(RT.uncheckedLongCast((Object)((Number)object20)), RT.uncheckedIntCast((Object)((Number)object21)), v, Long.MAX_VALUE / 4L - (object22 != null && object22 != Boolean.FALSE ? 0L : 1L));
        }
        return object;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return db$datum.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

