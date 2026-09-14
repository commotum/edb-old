/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$crosscheck_log_representations$fn__22167;

public final class integrity$crosscheck_log_representations
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"crosscheck-log-representations");
    public static final Var const__1 = RT.var((String)"datomic.integrity", (String)"progress-dot-fn");
    public static final Object const__2 = 1000L;
    public static final Var const__3 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__4 = RT.var((String)"datomic.tools", (String)"connection-resources");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__8 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__9 = RT.keyword(null, (String)"olookup");
    public static final Var const__10 = RT.var((String)"datomic.api", (String)"connect");
    public static final Var const__11 = RT.var((String)"datomic.api", (String)"db");
    public static final Var const__12 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__13 = RT.var((String)"datomic.log", (String)"create-log-val");
    public static final Var const__14 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__15 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Object const__16 = 0L;
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"vector");
    public static final Keyword const__20 = RT.keyword(null, (String)"basis-t");
    public static final Keyword const__22 = RT.keyword(null, (String)"index-basis-t");
    public static final Keyword const__24 = RT.keyword(null, (String)"tail-1");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__28 = RT.keyword(null, (String)"tail-2");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__31 = RT.keyword(null, (String)"diffs");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"basisT"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"tail"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"txes"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"tail"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"txes"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object uri2, Object progress) {
        Object object;
        Object object2;
        Object conn;
        Object object3;
        Object object4 = uri2;
        uri2 = null;
        Object uri3 = ((IFn)const__3.getRawRoot()).invoke(object4);
        Object map__22165 = ((IFn)const__4.getRawRoot()).invoke(uri3);
        Object object5 = ((IFn)const__5.getRawRoot()).invoke(map__22165);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__22165;
            map__22165 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__6.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__22165;
            map__22165 = null;
        }
        Object map__221652 = object3;
        Object cluster2 = RT.get((Object)map__221652, (Object)const__8);
        Object object7 = map__221652;
        map__221652 = null;
        Object olookup = RT.get((Object)object7, (Object)const__9);
        Object object8 = uri3;
        uri3 = null;
        Object object9 = conn = ((IFn)const__10.getRawRoot()).invoke(object8);
        conn = null;
        Object db2 = ((IFn)const__11.getRawRoot()).invoke(object9);
        Object log_1 = ((IFn)const__12.getRawRoot()).invoke(cluster2, olookup);
        Object object10 = cluster2;
        cluster2 = null;
        Object object11 = olookup;
        olookup = null;
        Object log_2 = ((IFn)const__13.getRawRoot()).invoke(object10, object11, db2);
        Object rep_1 = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(log_1, const__16));
        Object rep_2 = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(log_2, const__16));
        Object object12 = progress;
        progress = null;
        Object object13 = rep_1;
        rep_1 = null;
        Object object14 = rep_2;
        rep_2 = null;
        Object diffs = ((IFn)const__17.getRawRoot()).invoke((Object)new integrity$crosscheck_log_representations$fn__22167(object12), ((IFn)const__18.getRawRoot()).invoke(const__19.getRawRoot(), object13, object14));
        Object[] objectArray = new Object[8];
        objectArray[0] = const__20;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object15 = db2;
        Object object16 = iLookupThunk.get(object15);
        if (iLookupThunk == object16) {
            __thunk__0__ = __site__0__.fault(object15);
            object16 = __thunk__0__.get(object15);
        }
        objectArray[1] = object16;
        objectArray[2] = const__22;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object17 = db2;
        db2 = null;
        Object object18 = iLookupThunk2.get(object17);
        if (iLookupThunk2 == object18) {
            __thunk__1__ = __site__1__.fault(object17);
            object18 = __thunk__1__.get(object17);
        }
        objectArray[3] = object18;
        objectArray[4] = const__24;
        IFn iFn = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__3__;
        ILookupThunk iLookupThunk4 = __thunk__2__;
        Object object19 = log_1;
        log_1 = null;
        Object object20 = iLookupThunk4.get(object19);
        if (iLookupThunk4 == object20) {
            __thunk__2__ = __site__2__.fault(object19);
            object20 = __thunk__2__.get(object19);
        }
        if (iLookupThunk3 == (object2 = iLookupThunk3.get(object20))) {
            __thunk__3__ = __site__3__.fault(object20);
            object2 = __thunk__3__.get(object20);
        }
        objectArray[5] = iFn.invoke(object2);
        objectArray[6] = const__28;
        IFn iFn2 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk5 = __thunk__5__;
        ILookupThunk iLookupThunk6 = __thunk__4__;
        Object object21 = log_2;
        log_2 = null;
        Object object22 = iLookupThunk6.get(object21);
        if (iLookupThunk6 == object22) {
            __thunk__4__ = __site__4__.fault(object21);
            object22 = __thunk__4__.get(object21);
        }
        if (iLookupThunk5 == (object = iLookupThunk5.get(object22))) {
            __thunk__5__ = __site__5__.fault(object22);
            object = __thunk__5__.get(object22);
        }
        objectArray[7] = iFn2.invoke(object);
        IPersistentMap desc = RT.mapUniqueKeys((Object[])objectArray);
        Object object23 = ((IFn)const__6.getRawRoot()).invoke(diffs);
        if (object23 != null && object23 != Boolean.FALSE) {
            IPersistentMap iPersistentMap = desc;
            desc = null;
            Object object24 = diffs;
            diffs = null;
            throw (Throwable)((IFn)const__29.getRawRoot()).invoke((Object)"Log representations did not match", ((IFn)const__30.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__31, object24));
        }
        IPersistentMap iPersistentMap = desc;
        desc = null;
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$crosscheck_log_representations.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(const__2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$crosscheck_log_representations.invokeStatic(object2);
    }
}

