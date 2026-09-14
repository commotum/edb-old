/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.peer.TWatcher;
import java.util.Queue;

public final class TWatcherImpl
implements TWatcher,
IType {
    public final Object q;
    public final Object db_ref;
    public final Object f;
    public final Object lck;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__9;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Keyword const__14;
    public static final Var const__15;
    public static final Var const__17;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public TWatcherImpl(Object object, Object object2, Object object3, Object object4) {
        this.q = object;
        this.db_ref = object2;
        this.f = object3;
        this.lck = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"q")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.Queue")})), (Object)Symbol.intern(null, (String)"db-ref"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"lck"));
    }

    public Object release_pending_syncs(Object new_db) {
        Object var14_12;
        Object lockee__5436__auto__21466 = this.lck;
        try {
            synchronized (lockee__5436__auto__21466) {
                Object object;
                Object G__21461;
                Object map__21462 = G__21461 = ((Queue)this.q).peek();
                Object object2 = ((IFn)const__15.getRawRoot()).invoke(map__21462);
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object e = map__21462;
                    map__21462 = null;
                    object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__11.getRawRoot()).invoke(e)));
                } else {
                    object = map__21462;
                    map__21462 = null;
                }
                Object map__214622 = object;
                RT.get(map__214622, (Object)const__1);
                Object e = map__214622;
                map__214622 = null;
                RT.get(e, (Object)const__2);
                Object e2 = G__21461;
                G__21461 = null;
                Object G__214612 = e2;
                while (true) {
                    Object object3;
                    Object and__5236__auto__21465;
                    Object object4;
                    Object e3 = G__214612;
                    G__214612 = null;
                    Object map__21463 = e3;
                    Object object5 = ((IFn)const__15.getRawRoot()).invoke(map__21463);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        Object e4 = map__21463;
                        map__21463 = null;
                        object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__11.getRawRoot()).invoke(e4)));
                    } else {
                        object4 = map__21463;
                        map__21463 = null;
                    }
                    Object map__214632 = object4;
                    Object t = RT.get(map__214632, (Object)const__1);
                    Object e5 = map__214632;
                    map__214632 = null;
                    Object prom = RT.get(e5, (Object)const__2);
                    Object object6 = and__5236__auto__21465 = t;
                    if (object6 != null && object6 != Boolean.FALSE) {
                        Object object7 = t;
                        t = null;
                        object3 = Numbers.lte((Object)object7, (Object)((IFn)this.f).invoke(new_db)) ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        object3 = and__5236__auto__21465;
                        and__5236__auto__21465 = null;
                    }
                    if (object3 == null || object3 == Boolean.FALSE) break;
                    Object object8 = prom;
                    prom = null;
                    ((IFn)const__17.getRawRoot()).invoke(object8, new_db);
                    ((Queue)this.q).remove();
                    G__214612 = ((Queue)this.q).peek();
                }
                var14_12 = null;
            }
        }
        finally {
            Object object = lockee__5436__auto__21466;
            lockee__5436__auto__21466 = null;
            // ** MonitorExit[v14] (shouldn't be in output)
        }
        {
            return var14_12;
        }
    }

    /*
     * Unable to fully structure code
     */
    public Object sync_background_t(Object btype, Object t_or_tx) {
        block20: {
            v0 = t_or_tx;
            t_or_tx = null;
            t = ((IFn.LL)TWatcherImpl.const__3.getRawRoot()).invokePrim(RT.longCast((Object)((Number)v0)));
            v1 = and__5236__auto__21467 = (db = ((IFn)TWatcherImpl.const__4.getRawRoot()).invoke(this.db_ref));
            if (v1 != null && v1 != Boolean.FALSE) {
                v2 = Numbers.lte((long)t, (Object)((IFn)this.f).invoke(db)) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                v2 = and__5236__auto__21467;
                and__5236__auto__21467 = null;
            }
            if (v2 == null || v2 == Boolean.FALSE) break block20;
            v3 = db;
            db = null;
            this = null;
            v4 = ((IFn)TWatcherImpl.const__6.getRawRoot()).invoke(v3);
            ** GOTO lbl109
        }
        lockee__5436__auto__21471 = this.lck;
        try {
            synchronized (lockee__5436__auto__21471) {
                block22: {
                    block25: {
                        block23: {
                            block24: {
                                block21: {
                                    v5 = and__5236__auto__21468 = db;
                                    if (v5 != null && v5 != Boolean.FALSE) {
                                        v6 = Numbers.lte((long)t, (Object)((IFn)this.f).invoke(db)) ? Boolean.TRUE : Boolean.FALSE;
                                    } else {
                                        v6 = and__5236__auto__21468;
                                        and__5236__auto__21468 = null;
                                    }
                                    if (v6 == null || v6 == Boolean.FALSE) break block21;
                                    v7 = db;
                                    db = null;
                                    v8 = ((IFn)TWatcherImpl.const__6.getRawRoot()).invoke(v7);
                                    break block22;
                                }
                                v9 = and__5236__auto__21469 = db;
                                if (v9 != null && v9 != Boolean.FALSE) {
                                    v10 = TWatcherImpl.__thunk__0__;
                                    v11 = db;
                                    v12 = v10.get(v11);
                                    if (v10 == v12) {
                                        TWatcherImpl.__thunk__0__ = TWatcherImpl.__site__0__.fault(v11);
                                        v12 = TWatcherImpl.__thunk__0__.get(v11);
                                    }
                                    v13 = Numbers.lte((long)t, (Object)v12) ? Boolean.TRUE : Boolean.FALSE;
                                } else {
                                    v13 = and__5236__auto__21469;
                                    and__5236__auto__21469 = null;
                                }
                                if (v13 == null || v13 == Boolean.FALSE) break block23;
                                v14 = btype;
                                btype = null;
                                G__21459 = ((IFn)TWatcherImpl.const__9.getRawRoot()).invoke(db, v14, (Object)Numbers.num((long)t));
                                if (Util.identical((Object)G__21459, null)) {
                                    v15 = null;
                                } else {
                                    G__21459 = null;
                                    v15 = G__21459 = ((IFn)TWatcherImpl.const__11.getRawRoot()).invoke(G__21459);
                                }
                                if (Util.identical(G__21459, null)) {
                                    v16 = null;
                                } else {
                                    v17 = G__21459;
                                    G__21459 = null;
                                    v16 = ((IFn)TWatcherImpl.const__12.getRawRoot()).invoke(TWatcherImpl.const__13.getRawRoot(), v17);
                                }
                                v18 = temp__5455__auto__21470 = v16;
                                if (v18 == null || v18 == Boolean.FALSE) break block24;
                                v19 = temp__5455__auto__21470;
                                temp__5455__auto__21470 = null;
                                actual_t = v19;
                                v20 = this;
                                if (Util.classOf((Object)v20) == TWatcherImpl.__cached_class__1) ** GOTO lbl71
                                if (!(v20 instanceof TWatcher)) {
                                    v20 = v20;
                                    TWatcherImpl.__cached_class__1 = Util.classOf((Object)v20);
lbl71:
                                    // 2 sources

                                    v21 = actual_t;
                                    actual_t = null;
                                    v8 = TWatcherImpl.const__7.getRawRoot().invoke((Object)v20, v21);
                                } else {
                                    v22 = actual_t;
                                    actual_t = null;
                                    v8 = ((TWatcher)v20).wait_for_future_t(v22);
                                }
                                break block22;
                            }
                            v23 = db;
                            db = null;
                            v8 = ((IFn)TWatcherImpl.const__6.getRawRoot()).invoke(v23);
                            break block22;
                        }
                        v24 = TWatcherImpl.const__14;
                        if (v24 == null || v24 == Boolean.FALSE) break block25;
                        v25 = this;
                        if (Util.classOf((Object)v25) == TWatcherImpl.__cached_class__2) ** GOTO lbl92
                        if (!(v25 instanceof TWatcher)) {
                            v25 = v25;
                            TWatcherImpl.__cached_class__2 = Util.classOf((Object)v25);
lbl92:
                            // 2 sources

                            v8 = TWatcherImpl.const__7.getRawRoot().invoke((Object)v25, (Object)Numbers.num((long)t));
                        } else {
                            v8 = ((TWatcher)v25).wait_for_future_t(Numbers.num((long)t));
                        }
                        break block22;
                    }
                    v8 = null;
                }
                var9_8 = v8;
            }
        }
        finally {
            v26 = lockee__5436__auto__21471;
            lockee__5436__auto__21471 = null;
            // ** MonitorExit[v26] (shouldn't be in output)
        }
        {
            v4 = var9_8;
lbl109:
            // 2 sources

            return v4;
        }
    }

    /*
     * Unable to fully structure code
     */
    public Object sync_t(Object t_or_tx) {
        block11: {
            v0 = t_or_tx;
            t_or_tx = null;
            t = ((IFn.LL)TWatcherImpl.const__3.getRawRoot()).invokePrim(RT.longCast((Object)((Number)v0)));
            v1 = and__5236__auto__21472 = (db = ((IFn)TWatcherImpl.const__4.getRawRoot()).invoke(this.db_ref));
            if (v1 != null && v1 != Boolean.FALSE) {
                v2 = Numbers.lte((long)t, (Object)((IFn)this.f).invoke(db)) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                v2 = and__5236__auto__21472;
                and__5236__auto__21472 = null;
            }
            if (v2 == null || v2 == Boolean.FALSE) break block11;
            v3 = db;
            db = null;
            this = null;
            v4 = ((IFn)TWatcherImpl.const__6.getRawRoot()).invoke(v3);
            ** GOTO lbl52
        }
        lockee__5436__auto__21474 = this.lck;
        try {
            synchronized (lockee__5436__auto__21474) {
                block13: {
                    block12: {
                        v5 = and__5236__auto__21473 = db;
                        if (v5 != null && v5 != Boolean.FALSE) {
                            v6 = Numbers.lte((long)t, (Object)((IFn)this.f).invoke(db)) ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            v6 = and__5236__auto__21473;
                            and__5236__auto__21473 = null;
                        }
                        if (v6 == null || v6 == Boolean.FALSE) break block12;
                        v7 = db;
                        db = null;
                        v8 = ((IFn)TWatcherImpl.const__6.getRawRoot()).invoke(v7);
                        break block13;
                    }
                    v9 = this;
                    if (Util.classOf((Object)v9) == TWatcherImpl.__cached_class__0) ** GOTO lbl38
                    if (!(v9 instanceof TWatcher)) {
                        v9 = v9;
                        TWatcherImpl.__cached_class__0 = Util.classOf((Object)v9);
lbl38:
                        // 2 sources

                        v8 = TWatcherImpl.const__7.getRawRoot().invoke((Object)v9, (Object)Numbers.num((long)t));
                    } else {
                        v8 = ((TWatcher)v9).wait_for_future_t(Numbers.num((long)t));
                    }
                }
                var7_6 = v8;
            }
        }
        finally {
            v10 = lockee__5436__auto__21474;
            lockee__5436__auto__21474 = null;
            // ** MonitorExit[v10] (shouldn't be in output)
        }
        {
            v4 = var7_6;
lbl52:
            // 2 sources

            return v4;
        }
    }

    public Object wait_for_future_t(Object t) {
        Object prom = ((IFn)const__0.getRawRoot()).invoke();
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        Object object = t;
        t = null;
        objectArray[1] = object;
        objectArray[2] = const__2;
        objectArray[3] = prom;
        Boolean bl = ((Queue)this.q).add(RT.mapUniqueKeys((Object[])objectArray)) ? Boolean.TRUE : Boolean.FALSE;
        Object var2_2 = null;
        return prom;
    }

    static {
        const__0 = RT.var((String)"datomic.promise", (String)"settable-future");
        const__1 = RT.keyword(null, (String)"t");
        const__2 = RT.keyword(null, (String)"prom");
        const__3 = RT.var((String)"datomic.db", (String)"eid->eidx");
        const__4 = RT.var((String)"clojure.core", (String)"deref");
        const__6 = RT.var((String)"datomic.promise", (String)"delivered");
        const__7 = RT.var((String)"datomic.peer", (String)"wait-for-future-t");
        const__9 = RT.var((String)"datomic.db", (String)"ts-needing-index");
        const__11 = RT.var((String)"clojure.core", (String)"seq");
        const__12 = RT.var((String)"clojure.core", (String)"apply");
        const__13 = RT.var((String)"clojure.core", (String)"max");
        const__14 = RT.keyword(null, (String)"default");
        const__15 = RT.var((String)"clojure.core", (String)"seq?");
        const__17 = RT.var((String)"clojure.core", (String)"deliver");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"basisT"));
        __thunk__0__ = __site__0__;
    }
}

