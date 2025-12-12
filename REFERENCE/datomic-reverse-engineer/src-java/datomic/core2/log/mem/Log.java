/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.log.mem;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.log.mem.Log$fn__20873;
import datomic.core2.log.mem.Log$fn__20878;
import datomic.core2.log.spi.Append;
import datomic.core2.log.spi.Item;
import datomic.core2.log.spi.Scan;

public final class Log
implements Append,
Item,
Scan,
IType {
    public final Object items_ref;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap-vals!");
    public static final Object const__2 = 0L;
    public static final Var const__4 = RT.var((String)"datomic.core2.log.spi", (String)"result");
    public static final Keyword const__6 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__7 = RT.keyword((String)"cognitect.anomalies", (String)"conflict");
    public static final Keyword const__8 = RT.keyword((String)"cognitect.anomalies", (String)"message");
    public static final Keyword const__9 = RT.keyword((String)"datomic.core2.log.mem", (String)"tail");
    public static final Keyword const__10 = RT.keyword(null, (String)"header");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"peek");
    public static final Keyword const__12 = RT.keyword((String)"datomic.core2.log.mem", (String)"header");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__19 = RT.keyword(null, (String)"t");
    public static final Keyword const__20 = RT.keyword(null, (String)"ch");
    public static final Keyword const__21 = RT.keyword(null, (String)"limit");
    public static final Keyword const__22 = RT.keyword(null, (String)"direction");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__26 = RT.var((String)"datomic.core2.algo.search", (String)"binary-search");
    public static final Keyword const__27 = RT.keyword(null, (String)"next-t");
    public static final Var const__28 = RT.var((String)"datomic.core2.algo.search", (String)"fn-comparator");
    public static final Keyword const__29 = RT.keyword(null, (String)"forward");
    public static final Var const__30 = RT.var((String)"clojure.core.async", (String)"onto-chan!");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"subvec");
    public static final Keyword const__33 = RT.keyword(null, (String)"backward");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"rseq");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"header"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"header"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"body"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public Log(Object object) {
        this.items_ref = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"items-ref"));
    }

    @Override
    public Object _item_body(Object item) {
        IFn iFn = (IFn)const__4.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__2__;
        Object object = item;
        item = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__2__ = __site__2__.fault(object);
            object2 = __thunk__2__.get(object);
        }
        Log this_ = null;
        return iFn.invoke(object2);
    }

    @Override
    public Object _item_header(Object item) {
        ILookupThunk iLookupThunk = __thunk__1__;
        Object object = item;
        item = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__1__ = __site__1__.fault(object);
            object2 = __thunk__1__.get(object);
        }
        return object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public Object _scan(Object opts) {
        Number number;
        Object object;
        block14: {
            Object map__20877;
            block15: {
                Object object2 = opts;
                opts = null;
                map__20877 = object2;
                Object object3 = ((IFn)const__13.getRawRoot()).invoke(map__20877);
                if (object3 == null || object3 == Boolean.FALSE) break block15;
                Object object4 = ((IFn)const__14.getRawRoot()).invoke(map__20877);
                if (object4 != null && object4 != Boolean.FALSE) {
                    Object object5 = map__20877;
                    map__20877 = null;
                    object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__15.getRawRoot()).invoke(object5)));
                    break block14;
                } else {
                    Object object6 = ((IFn)const__16.getRawRoot()).invoke(map__20877);
                    if (object6 != null && object6 != Boolean.FALSE) {
                        Object object7 = map__20877;
                        map__20877 = null;
                        object = ((IFn)const__17.getRawRoot()).invoke(object7);
                        break block14;
                    } else {
                        object = PersistentArrayMap.EMPTY;
                    }
                }
                break block14;
            }
            object = map__20877;
            map__20877 = null;
        }
        Object map__20877 = object;
        Object t = RT.get((Object)map__20877, (Object)const__19);
        Object ch = RT.get((Object)map__20877, (Object)const__20);
        Object limit2 = RT.get((Object)map__20877, (Object)const__21);
        Object object8 = map__20877;
        map__20877 = null;
        Object direction = RT.get((Object)object8, (Object)const__22);
        if (Util.equiv((long)Long.MAX_VALUE, (Object)t)) {
            number = Numbers.num((long)Long.MAX_VALUE);
        } else {
            Object object9 = t;
            t = null;
            number = Numbers.inc((Object)object9);
        }
        Number next_t2 = number;
        Object items = ((IFn)const__24.getRawRoot()).invoke(this_.items_ref);
        int ct = RT.count((Object)items);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__10;
        Object[] objectArray2 = new Object[2];
        objectArray2[0] = const__27;
        Number number2 = next_t2;
        next_t2 = null;
        objectArray2[1] = number2;
        objectArray[1] = RT.mapUniqueKeys((Object[])objectArray2);
        Object idx = ((IFn)const__26.getRawRoot()).invoke(items, (Object)RT.mapUniqueKeys((Object[])objectArray), ((IFn)const__28.getRawRoot()).invoke((Object)new Log$fn__20878()));
        Object object10 = direction;
        direction = null;
        Object G__20880 = object10;
        switch (Util.hash((Object)G__20880) >> 0 & 1) {
            case 0: {
                Object object11;
                if (G__20880 != const__29) break;
                IFn iFn = (IFn)const__30.getRawRoot();
                Object object12 = ch;
                ch = null;
                IFn iFn2 = (IFn)const__31.getRawRoot();
                Object object13 = limit2;
                limit2 = null;
                Object object14 = idx;
                if (object14 != null && object14 != Boolean.FALSE) {
                    Object object15 = items;
                    items = null;
                    Object object16 = idx;
                    idx = null;
                    object11 = ((IFn)const__32.getRawRoot()).invoke(object15, object16);
                } else {
                    object11 = PersistentVector.EMPTY;
                }
                Log this_ = null;
                Object object17 = iFn.invoke(object12, iFn2.invoke(object13, object11));
                return object17;
            }
            case 1: {
                Object object18;
                if (G__20880 != const__33) break;
                IFn iFn = (IFn)const__30.getRawRoot();
                Object object19 = ch;
                ch = null;
                IFn iFn3 = (IFn)const__31.getRawRoot();
                Object object20 = limit2;
                limit2 = null;
                IFn iFn4 = (IFn)const__34.getRawRoot();
                Object object21 = idx;
                if (object21 != null && object21 != Boolean.FALSE) {
                    Object object22 = items;
                    items = null;
                    Object object23 = idx;
                    idx = null;
                    object18 = ((IFn)const__32.getRawRoot()).invoke(object22, const__2, (Object)Numbers.inc((Object)object23));
                } else {
                    object18 = items;
                    items = null;
                }
                Log this_ = null;
                Object object17 = iFn.invoke(object19, iFn3.invoke(object20, iFn4.invoke(object18)));
                return object17;
            }
        }
        Object object24 = G__20880;
        G__20880 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__35.getRawRoot()).invoke((Object)"No matching clause: ", object24));
    }

    @Override
    public Object _append(Object header, Object body) {
        Object object;
        Object object2 = body;
        body = null;
        Object vec__20870 = ((IFn)const__0.getRawRoot()).invoke(this_.items_ref, (Object)new Log$fn__20873(header, object2));
        Object old = RT.nth((Object)vec__20870, (int)RT.intCast((long)0L), null);
        Object object3 = vec__20870;
        vec__20870 = null;
        Object object4 = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        IFn iFn = (IFn)const__4.getRawRoot();
        Object object5 = object4;
        object4 = null;
        if (Util.equiv((Object)old, (Object)object5)) {
            Object[] objectArray = new Object[8];
            objectArray[0] = const__6;
            objectArray[1] = const__7;
            objectArray[2] = const__8;
            objectArray[3] = "Not a continuation of the log";
            objectArray[4] = const__9;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = old;
            old = null;
            Object object7 = ((IFn)const__11.getRawRoot()).invoke(object6);
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__0__ = __site__0__.fault(object7);
                object8 = __thunk__0__.get(object7);
            }
            objectArray[5] = object8;
            objectArray[6] = const__12;
            Object object9 = header;
            header = null;
            objectArray[7] = object9;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = header;
            header = null;
        }
        Log this_ = null;
        return iFn.invoke(object);
    }
}

