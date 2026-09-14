/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
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
import clojure.lang.Util;
import clojure.lang.Var;

public final class db$reverse_datum_spec
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"t");
    public static final Keyword const__7 = RT.keyword(null, (String)"asserting");
    public static final Keyword const__9 = RT.keyword(null, (String)"aevt");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Keyword const__14 = RT.keyword(null, (String)"avet");
    public static final Keyword const__15 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__16 = RT.keyword(null, (String)"vaet");
    public static final Object const__17 = 0L;

    public static Object invokeStatic(Object db2, Object index2, ISeq p__12873) {
        Object object;
        Object or__5238__auto__12884;
        Object object2;
        Object object3;
        boolean and__5236__auto__12880;
        boolean and__5236__auto__12881;
        Object object4;
        boolean and__5236__auto__12876;
        ISeq iSeq;
        ISeq iSeq2 = p__12873;
        p__12873 = null;
        ISeq map__12874 = iSeq2;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke((Object)map__12874);
        if (object5 != null && object5 != Boolean.FALSE) {
            ISeq iSeq3 = map__12874;
            map__12874 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12874;
            map__12874 = null;
        }
        ISeq map__128742 = iSeq;
        Object e = RT.get((Object)map__128742, (Object)const__3);
        Object a = RT.get((Object)map__128742, (Object)const__4);
        Object v = RT.get((Object)map__128742, (Object)const__5);
        Object t = RT.get((Object)map__128742, (Object)const__6);
        ISeq iSeq4 = map__128742;
        map__128742 = null;
        Object asserting = RT.get((Object)iSeq4, (Object)const__7, (Object)Boolean.FALSE);
        boolean and__5236__auto__12877 = Util.equiv((Object)index2, (Object)const__9);
        Object object6 = and__5236__auto__12877 ? ((and__5236__auto__12876 = Util.identical((Object)v, null)) ? ((IFn)const__11.getRawRoot()).invoke((Object)(Util.identical((Object)e, null) ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__12876 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__12877 ? Boolean.TRUE : Boolean.FALSE);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = e;
            e = null;
            object4 = Numbers.unchecked_inc((Object)((IFn)const__13.getRawRoot()).invoke(db2, object7));
        } else {
            Object or__5238__auto__12878;
            Object object8 = e;
            e = null;
            Object object9 = or__5238__auto__12878 = ((IFn)const__13.getRawRoot()).invoke(db2, object8);
            if (object9 != null && object9 != Boolean.FALSE) {
                object4 = or__5238__auto__12878;
                or__5238__auto__12878 = null;
            } else {
                object4 = Numbers.num((long)Long.MAX_VALUE);
            }
        }
        Number e2 = object4;
        boolean or__5238__auto__12879 = Util.equiv((Object)index2, (Object)const__14);
        boolean bl = and__5236__auto__12881 = or__5238__auto__12879 ? or__5238__auto__12879 : Util.equiv((Object)index2, (Object)const__15);
        Object object10 = and__5236__auto__12881 ? ((and__5236__auto__12880 = Util.identical((Object)v, null)) ? ((IFn)const__11.getRawRoot()).invoke((Object)(Util.identical((Object)a, null) ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__12880 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__12881 ? Boolean.TRUE : Boolean.FALSE);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = db2;
            db2 = null;
            Object object12 = a;
            a = null;
            object3 = Numbers.unchecked_inc((Object)((IFn)const__13.getRawRoot()).invoke(object11, object12));
        } else {
            Object or__5238__auto__12882;
            Object object13 = db2;
            db2 = null;
            Object object14 = a;
            a = null;
            Object object15 = or__5238__auto__12882 = ((IFn)const__13.getRawRoot()).invoke(object13, object14);
            if (object15 != null && object15 != Boolean.FALSE) {
                object3 = or__5238__auto__12882;
                or__5238__auto__12882 = null;
            } else {
                object3 = Integer.MAX_VALUE;
            }
        }
        Number a2 = object3;
        Object object16 = index2;
        index2 = null;
        boolean and__5236__auto__12883 = Util.equiv((Object)object16, (Object)const__16);
        Object object17 = and__5236__auto__12883 ? ((IFn)const__11.getRawRoot()).invoke(v) : (and__5236__auto__12883 ? Boolean.TRUE : Boolean.FALSE);
        if (object17 != null && object17 != Boolean.FALSE) {
            object2 = Numbers.num((long)Long.MAX_VALUE);
        } else {
            object2 = v;
            v = null;
        }
        Object v2 = object2;
        Object object18 = t;
        t = null;
        Object object19 = or__5238__auto__12884 = object18;
        if (object19 != null && object19 != Boolean.FALSE) {
            object = or__5238__auto__12884;
            or__5238__auto__12884 = null;
        } else {
            object = const__17;
        }
        Object t2 = object;
        Object[] objectArray = new Object[10];
        objectArray[0] = const__3;
        Number number = e2;
        e2 = null;
        objectArray[1] = number;
        objectArray[2] = const__4;
        Number number2 = a2;
        a2 = null;
        objectArray[3] = number2;
        objectArray[4] = const__5;
        Object object20 = v2;
        v2 = null;
        objectArray[5] = object20;
        objectArray[6] = const__6;
        Object object21 = t2;
        t2 = null;
        objectArray[7] = object21;
        objectArray[8] = const__7;
        Object object22 = asserting;
        asserting = null;
        objectArray[9] = object22;
        return RT.vector((Object[])objectArray);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return db$reverse_datum_spec.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

