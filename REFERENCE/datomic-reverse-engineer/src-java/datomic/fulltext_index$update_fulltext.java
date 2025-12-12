/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.fulltext_index$update_fulltext$fn__12377;
import datomic.fulltext_index.PersistentFulltext;
import datomic.impl.db.IDatum;

public final class fulltext_index$update_fulltext
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Keyword const__4 = RT.keyword(null, (String)"writer");
    public static final Var const__5 = RT.var((String)"datomic.lucene", (String)"persistent-directory");
    public static final Keyword const__6 = RT.keyword(null, (String)"directory");
    public static final Var const__7 = RT.var((String)"datomic.lucene", (String)"index-writer");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__12 = RT.var((String)"datomic.lucene", (String)"add-document");
    public static final Var const__13 = RT.var((String)"datomic.fulltext-index", (String)"datum->doc");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"reduce");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"writer"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"writer"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object pft, Object data2) {
        PersistentArrayMap tmap;
        Object G__12366;
        Object vec__12367;
        Object object;
        Object or__5238__auto__12384;
        Object object2 = pft;
        pft = null;
        Object object3 = or__5238__auto__12384 = object2;
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__12384;
            or__5238__auto__12384 = null;
        } else {
            object = new PersistentFulltext();
        }
        Object pft2 = object;
        PersistentArrayMap tmap2 = PersistentArrayMap.EMPTY;
        Object object4 = data2;
        data2 = null;
        Object object5 = vec__12367 = (G__12366 = object4);
        vec__12367 = null;
        Object seq__12368 = ((IFn)const__0.getRawRoot()).invoke(object5);
        Object first__12369 = ((IFn)const__1.getRawRoot()).invoke(seq__12368);
        Object object6 = seq__12368;
        seq__12368 = null;
        Object seq__123682 = ((IFn)const__2.getRawRoot()).invoke(object6);
        first__12369 = null;
        seq__123682 = null;
        PersistentArrayMap persistentArrayMap = tmap2;
        tmap2 = null;
        Object tmap3 = persistentArrayMap;
        Object object7 = G__12366;
        G__12366 = null;
        Object G__123662 = object7;
        while (true) {
            Object writer2;
            IPersistentVector iPersistentVector;
            Object temp__5455__auto__12385;
            Object vec__12370;
            PersistentArrayMap persistentArrayMap2 = tmap3;
            tmap3 = null;
            tmap = persistentArrayMap2;
            Object object8 = G__123662;
            G__123662 = null;
            Object object9 = vec__12370 = object8;
            vec__12370 = null;
            Object seq__12371 = ((IFn)const__0.getRawRoot()).invoke(object9);
            Object first__12372 = ((IFn)const__1.getRawRoot()).invoke(seq__12371);
            Object object10 = seq__12371;
            seq__12371 = null;
            Object seq__123712 = ((IFn)const__2.getRawRoot()).invoke(object10);
            Object object11 = first__12372;
            first__12372 = null;
            Object datum2 = object11;
            Object object12 = seq__123712;
            seq__123712 = null;
            Object more = object12;
            Object object13 = datum2;
            if (object13 == null || object13 == Boolean.FALSE) break;
            int a = ((IDatum)datum2).getA();
            Object object14 = temp__5455__auto__12385 = RT.get((Object)tmap, (Object)a);
            if (object14 != null && object14 != Boolean.FALSE) {
                Object object15 = temp__5455__auto__12385;
                temp__5455__auto__12385 = null;
                Object entry = object15;
                PersistentArrayMap persistentArrayMap3 = tmap;
                tmap = null;
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object16 = entry;
                entry = null;
                Object object17 = iLookupThunk.get(object16);
                if (iLookupThunk == object17) {
                    __thunk__0__ = __site__0__.fault(object16);
                    object17 = __thunk__0__.get(object16);
                }
                iPersistentVector = Tuple.create((Object)persistentArrayMap3, (Object)object17);
            } else {
                Object dir = ((IFn)const__5.getRawRoot()).invoke(RT.get((Object)pft2, (Object)a, (Object)PersistentArrayMap.EMPTY));
                Object[] objectArray = new Object[4];
                objectArray[0] = const__6;
                objectArray[1] = dir;
                objectArray[2] = const__4;
                Object object18 = dir;
                dir = null;
                objectArray[3] = ((IFn)const__7.getRawRoot()).invoke(object18);
                IPersistentMap entry = RT.mapUniqueKeys((Object[])objectArray);
                PersistentArrayMap persistentArrayMap4 = tmap;
                tmap = null;
                Object object19 = ((IFn)const__8.getRawRoot()).invoke((Object)persistentArrayMap4, (Object)a, (Object)entry);
                ILookupThunk iLookupThunk = __thunk__1__;
                IPersistentMap iPersistentMap = entry;
                entry = null;
                Object object20 = iLookupThunk.get((Object)iPersistentMap);
                if (iLookupThunk == object20) {
                    __thunk__1__ = __site__1__.fault((Object)iPersistentMap);
                    object20 = __thunk__1__.get((Object)iPersistentMap);
                }
                iPersistentVector = Tuple.create((Object)object19, (Object)object20);
            }
            IPersistentVector vec__12373 = iPersistentVector;
            Object tmap4 = RT.nth((Object)vec__12373, (int)RT.intCast((long)0L), null);
            IPersistentVector iPersistentVector2 = vec__12373;
            vec__12373 = null;
            Object object21 = writer2 = RT.nth((Object)iPersistentVector2, (int)RT.intCast((long)1L), null);
            writer2 = null;
            Object object22 = datum2;
            datum2 = null;
            ((IFn)const__12.getRawRoot()).invoke(object21, ((IFn)const__13.getRawRoot()).invoke(object22));
            Object object23 = tmap4;
            tmap4 = null;
            Object object24 = more;
            more = null;
            G__123662 = object24;
            tmap3 = object23;
        }
        PersistentArrayMap persistentArrayMap5 = tmap;
        tmap = null;
        return ((IFn)const__14.getRawRoot()).invoke((Object)new fulltext_index$update_fulltext$fn__12377(), pft2, (Object)persistentArrayMap5);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext_index$update_fulltext.invokeStatic(object3, object4);
    }
}

