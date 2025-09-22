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
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.log.ddb;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.log.ddb.Log$fn__20770;
import datomic.core2.log.ddb.Log$next_r__20646;
import datomic.core2.log.ddb.Log$query__20644;
import datomic.core2.log.spi.Append;
import datomic.core2.log.spi.Item;
import datomic.core2.log.spi.Scan;

public final class Log
implements Append,
Item,
Scan,
IType {
    public final Object client;
    public final Object table;
    public final Object p;
    public final Object chunk_size;
    public static final Var const__0 = RT.var((String)"cognitect.aws.client.api", (String)"invoke-async");
    public static final Var const__1 = RT.var((String)"datomic.core2.log.ddb", (String)"append-request");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__10 = RT.keyword(null, (String)"ch");
    public static final Keyword const__11 = RT.keyword(null, (String)"direction");
    public static final Keyword const__12 = RT.keyword(null, (String)"limit");
    public static final Keyword const__13 = RT.keyword(null, (String)"t");
    public static final Var const__14 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__15 = 1L;
    public static final Var const__16 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"header"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"body"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Log(Object object, Object object2, Object object3, Object object4) {
        this.client = object;
        this.table = object2;
        this.p = object3;
        this.chunk_size = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"p"), (Object)Symbol.intern(null, (String)"chunk-size"));
    }

    @Override
    public Object _scan(Object opts) {
        Object object;
        Object map__20643 = opts;
        Object object2 = ((IFn)const__4.getRawRoot()).invoke(map__20643);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__5.getRawRoot()).invoke(map__20643);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__20643;
                map__20643 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__6.getRawRoot()).invoke(object4)));
            } else {
                Object object5 = ((IFn)const__7.getRawRoot()).invoke(map__20643);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__20643;
                    map__20643 = null;
                    object = ((IFn)const__8.getRawRoot()).invoke(object6);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20643;
            map__20643 = null;
        }
        Object map__206432 = object;
        Object ch = RT.get((Object)map__206432, (Object)const__10);
        Object direction = RT.get((Object)map__206432, (Object)const__11);
        Object limit2 = RT.get((Object)map__206432, (Object)const__12);
        Object t = RT.get((Object)map__206432, (Object)const__13);
        Log$query__20644 query2 = new Log$query__20644(this.table, this.p, this.client);
        Log$next_r__20646 next_r = new Log$next_r__20646(direction);
        Object c__10230__auto__20856 = ((IFn)const__14.getRawRoot()).invoke(const__15);
        Object captured_bindings__10231__auto__20857 = Var.getThreadBindingFrame();
        Object object7 = limit2;
        limit2 = null;
        Log$query__20644 log$query__20644 = query2;
        query2 = null;
        Object object8 = map__206432;
        map__206432 = null;
        Object object9 = ch;
        ch = null;
        Object object10 = direction;
        direction = null;
        Log$next_r__20646 log$next_r__20646 = next_r;
        next_r = null;
        Object object11 = opts;
        opts = null;
        Object object12 = t;
        t = null;
        Object object13 = captured_bindings__10231__auto__20857;
        captured_bindings__10231__auto__20857 = null;
        ((IFn)const__16.getRawRoot()).invoke((Object)new Log$fn__20770(object7, (Object)log$query__20644, object8, object9, object10, (Object)log$next_r__20646, this.table, this, c__10230__auto__20856, this.chunk_size, object11, this.p, object12, object13, this.client));
        Object object14 = c__10230__auto__20856;
        c__10230__auto__20856 = null;
        return object14;
    }

    @Override
    public Object _item_body(Object item) {
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

    @Override
    public Object _item_header(Object item) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = item;
        item = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return object2;
    }

    @Override
    public Object _append(Object header, Object body) {
        Object object = header;
        header = null;
        Object object2 = body;
        body = null;
        Log this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.client, ((IFn)const__1.getRawRoot()).invoke(this_.table, this_.p, object, object2));
    }
}

