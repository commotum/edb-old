/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
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
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;

public final class qtune$mapq__GT_listq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"find");
    public static final Keyword const__4 = RT.keyword(null, (String)"with");
    public static final Keyword const__5 = RT.keyword(null, (String)"in");
    public static final Keyword const__6 = RT.keyword(null, (String)"where");
    public static final Keyword const__7 = RT.keyword(null, (String)"timeout");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object p__23419) {
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object object5;
        Object object6;
        Object object7 = p__23419;
        p__23419 = null;
        Object map__23420 = object7;
        Object object8 = ((IFn)const__0.getRawRoot()).invoke(map__23420);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__23420;
            map__23420 = null;
            object6 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object9)));
        } else {
            object6 = map__23420;
            map__23420 = null;
        }
        Object map__234202 = object6;
        Object find = RT.get((Object)map__234202, (Object)const__3);
        Object with2 = RT.get((Object)map__234202, (Object)const__4);
        Object in = RT.get((Object)map__234202, (Object)const__5);
        Object where = RT.get((Object)map__234202, (Object)const__6);
        Object object10 = map__234202;
        map__234202 = null;
        Object timeout = RT.get((Object)object10, (Object)const__7);
        PersistentVector G__23421 = PersistentVector.EMPTY;
        Object object11 = find;
        if (object11 != null && object11 != Boolean.FALSE) {
            PersistentVector persistentVector = G__23421;
            G__23421 = null;
            Object object12 = find;
            find = null;
            object5 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)persistentVector, (Object)const__3), object12);
        } else {
            object5 = G__23421;
            G__23421 = null;
        }
        PersistentVector G__234212 = object5;
        Object object13 = with2;
        if (object13 != null && object13 != Boolean.FALSE) {
            PersistentVector persistentVector = G__234212;
            G__234212 = null;
            Object object14 = with2;
            with2 = null;
            object4 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)persistentVector, (Object)const__4), object14);
        } else {
            object4 = G__234212;
            G__234212 = null;
        }
        PersistentVector G__234213 = object4;
        Object object15 = in;
        if (object15 != null && object15 != Boolean.FALSE) {
            PersistentVector persistentVector = G__234213;
            G__234213 = null;
            Object object16 = in;
            in = null;
            object3 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)persistentVector, (Object)const__5), object16);
        } else {
            object3 = G__234213;
            G__234213 = null;
        }
        PersistentVector G__234214 = object3;
        Object object17 = where;
        if (object17 != null && object17 != Boolean.FALSE) {
            PersistentVector persistentVector = G__234214;
            G__234214 = null;
            Object object18 = where;
            where = null;
            object2 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)persistentVector, (Object)const__6), object18);
        } else {
            object2 = G__234214;
            G__234214 = null;
        }
        PersistentVector G__234215 = object2;
        Object object19 = timeout;
        if (object19 != null && object19 != Boolean.FALSE) {
            PersistentVector persistentVector = G__234215;
            G__234215 = null;
            Object object20 = timeout;
            timeout = null;
            object = ((IFn)const__9.getRawRoot()).invoke((Object)persistentVector, (Object)const__7, ((IFn)const__10.getRawRoot()).invoke(object20));
        } else {
            object = G__234215;
            G__234215 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return qtune$mapq__GT_listq.invokeStatic(object2);
    }
}

