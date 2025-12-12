/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$attrs_missing_hooks
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attr-hook-attr-ids");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Keyword const__7 = RT.keyword(null, (String)"a");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"conj!");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"schema-hook-attrs");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"disj!");
    public static final Keyword const__14 = RT.keyword(null, (String)"default");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"empty?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"added"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object datoms2) {
        Object object;
        block15: {
            Object eids;
            block14: {
                Object object2;
                Object G__13318;
                Object vec__13319;
                Object object3 = db2;
                db2 = null;
                Object attr_hook_attr_QMARK_ = ((IFn)const__0.getRawRoot()).invoke(object3);
                Object eids2 = ((IFn)const__1.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY);
                Object object4 = datoms2;
                datoms2 = null;
                Object object5 = vec__13319 = (G__13318 = object4);
                vec__13319 = null;
                Object seq__13320 = ((IFn)const__2.getRawRoot()).invoke(object5);
                Object first__13321 = ((IFn)const__3.getRawRoot()).invoke(seq__13320);
                Object object6 = seq__13320;
                seq__13320 = null;
                Object seq__133202 = ((IFn)const__4.getRawRoot()).invoke(object6);
                Object object7 = first__13321;
                first__13321 = null;
                Object map__13322 = object7;
                Object object8 = ((IFn)const__5.getRawRoot()).invoke(map__13322);
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = map__13322;
                    map__13322 = null;
                    object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object9)));
                } else {
                    object2 = map__13322;
                    map__13322 = null;
                }
                Object map__133222 = object2;
                Object object10 = map__133222;
                map__133222 = null;
                RT.get((Object)object10, (Object)const__7);
                seq__133202 = null;
                Object object11 = eids2;
                eids2 = null;
                Object eids3 = object11;
                Object object12 = G__13318;
                G__13318 = null;
                Object G__133182 = object12;
                while (true) {
                    Object object13;
                    Object and__5236__auto__13328;
                    Object map__13326;
                    Object object14;
                    Object vec__13323;
                    Object object15 = eids3;
                    eids3 = null;
                    eids = object15;
                    Object object16 = G__133182;
                    G__133182 = null;
                    Object object17 = vec__13323 = object16;
                    vec__13323 = null;
                    Object seq__13324 = ((IFn)const__2.getRawRoot()).invoke(object17);
                    Object first__13325 = ((IFn)const__3.getRawRoot()).invoke(seq__13324);
                    Object object18 = seq__13324;
                    seq__13324 = null;
                    Object seq__133242 = ((IFn)const__4.getRawRoot()).invoke(object18);
                    Object object19 = first__13325;
                    first__13325 = null;
                    Object map__133262 = object19;
                    Object object20 = ((IFn)const__5.getRawRoot()).invoke(map__133262);
                    if (object20 != null && object20 != Boolean.FALSE) {
                        Object object21 = map__133262;
                        map__133262 = null;
                        object14 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object21)));
                    } else {
                        object14 = map__133262;
                        map__133262 = null;
                    }
                    Object d = map__13326 = object14;
                    Object object22 = map__13326;
                    map__13326 = null;
                    Object a = RT.get((Object)object22, (Object)const__7);
                    Object object23 = seq__133242;
                    seq__133242 = null;
                    Object more = object23;
                    Object object24 = d;
                    if (object24 == null || object24 == Boolean.FALSE) break block14;
                    Object object25 = ((IFn)attr_hook_attr_QMARK_).invoke(a);
                    if (object25 != null && object25 != Boolean.FALSE) {
                        IFn iFn = (IFn)const__8.getRawRoot();
                        Object object26 = eids;
                        eids = null;
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object27 = d;
                        d = null;
                        Object object28 = iLookupThunk.get(object27);
                        if (iLookupThunk == object28) {
                            __thunk__0__ = __site__0__.fault(object27);
                            object28 = __thunk__0__.get(object27);
                        }
                        Object object29 = more;
                        more = null;
                        G__133182 = object29;
                        eids3 = iFn.invoke(object26, object28);
                        continue;
                    }
                    Object object30 = a;
                    a = null;
                    Object object31 = and__5236__auto__13328 = ((IFn)const__10.getRawRoot()).invoke(object30);
                    if (object31 != null && object31 != Boolean.FALSE) {
                        ILookupThunk iLookupThunk = __thunk__1__;
                        Object object32 = d;
                        object13 = iLookupThunk.get(object32);
                        if (iLookupThunk == object13) {
                            __thunk__1__ = __site__1__.fault(object32);
                            object13 = __thunk__1__.get(object32);
                        }
                    } else {
                        object13 = and__5236__auto__13328;
                        and__5236__auto__13328 = null;
                    }
                    if (object13 != null && object13 != Boolean.FALSE) {
                        IFn iFn = (IFn)const__12.getRawRoot();
                        Object object33 = eids;
                        eids = null;
                        ILookupThunk iLookupThunk = __thunk__2__;
                        Object object34 = d;
                        d = null;
                        Object object35 = iLookupThunk.get(object34);
                        if (iLookupThunk == object35) {
                            __thunk__2__ = __site__2__.fault(object34);
                            object35 = __thunk__2__.get(object34);
                        }
                        Object object36 = more;
                        more = null;
                        G__133182 = object36;
                        eids3 = iFn.invoke(object33, object35);
                        continue;
                    }
                    Keyword keyword = const__14;
                    if (keyword == null || keyword == Boolean.FALSE) break;
                    Object object37 = eids;
                    eids = null;
                    Object object38 = more;
                    more = null;
                    G__133182 = object38;
                    eids3 = object37;
                }
                object = null;
                break block15;
            }
            Object object39 = eids;
            eids = null;
            Object eids4 = ((IFn)const__15.getRawRoot()).invoke(object39);
            Object object40 = ((IFn)const__16.getRawRoot()).invoke(eids4);
            if (object40 != null && object40 != Boolean.FALSE) {
                object = null;
            } else {
                object = eids4;
                eids4 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$attrs_missing_hooks.invokeStatic(object3, object4);
    }
}

