/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.log.mem;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class Log$fn__20873
extends AFunction {
    Object header;
    Object body;
    public static final Keyword const__0 = RT.keyword(null, (String)"header");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"peek");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__8 = RT.keyword(null, (String)"t");
    public static final Keyword const__9 = RT.keyword(null, (String)"next-t");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__13 = RT.keyword(null, (String)"body");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"header"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Log$fn__20873(Object object, Object object2) {
        this.header = object;
        this.body = object2;
    }

    public Object invoke(Object items) {
        Object object;
        Object object2;
        Object or__5581__auto__20876;
        Object object3;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(items);
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        Object map__20874 = object5;
        Object object6 = ((IFn)const__2.getRawRoot()).invoke(map__20874);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__20874);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = map__20874;
                map__20874 = null;
                object3 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__4.getRawRoot()).invoke(object8)));
            } else {
                Object object9 = ((IFn)const__5.getRawRoot()).invoke(map__20874);
                if (object9 != null && object9 != Boolean.FALSE) {
                    Object object10 = map__20874;
                    map__20874 = null;
                    object3 = ((IFn)const__6.getRawRoot()).invoke(object10);
                } else {
                    object3 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object3 = map__20874;
            map__20874 = null;
        }
        Object map__208742 = object3;
        Object t = RT.get((Object)map__208742, (Object)const__8);
        Object object11 = map__208742;
        map__208742 = null;
        Object next_t2 = RT.get((Object)object11, (Object)const__9);
        Object object12 = t;
        t = null;
        Object object13 = or__5581__auto__20876 = ((IFn)const__10.getRawRoot()).invoke(object12);
        if (object13 != null && object13 != Boolean.FALSE) {
            object2 = or__5581__auto__20876;
            or__5581__auto__20876 = null;
        } else {
            Object object14 = next_t2;
            next_t2 = null;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object15 = this_.header;
            Object object16 = iLookupThunk2.get(object15);
            if (iLookupThunk2 == object16) {
                __thunk__1__ = __site__1__.fault(object15);
                object16 = __thunk__1__.get(object15);
            }
            object2 = Util.equiv((Object)object14, (Object)object16) ? Boolean.TRUE : Boolean.FALSE;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object17 = items;
            items = null;
            Log$fn__20873 this_ = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object17, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__0, this_.header, const__13, this_.body}));
        } else {
            object = items;
            Object var1_1 = null;
        }
        return object;
    }
}

