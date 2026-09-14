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
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;
import datomic.memory_size.MemorySize;
import org.slf4j.LoggerFactory;

public final class log$catchup_tx
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    public static final Var const__8;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Var const__16;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object p__16361, Object tx) {
        block7: {
            block6: {
                v0 = p__16361;
                p__16361 = null;
                map__16362 = v0;
                v1 = ((IFn)log$catchup_tx.const__0.getRawRoot()).invoke(map__16362);
                if (v1 != null && v1 != Boolean.FALSE) {
                    v2 = map__16362;
                    map__16362 = null;
                    v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)log$catchup_tx.const__1.getRawRoot()).invoke(v2)));
                } else {
                    v3 = map__16362;
                    map__16362 = null;
                }
                map__16362 = v3;
                db = RT.get((Object)map__16362, (Object)log$catchup_tx.const__3);
                v4 = map__16362;
                map__16362 = null;
                size = RT.get((Object)v4, (Object)log$catchup_tx.const__4);
                v5 = tx;
                tx = null;
                data = ((IFn)log$catchup_tx.const__5.getRawRoot()).invoke(v5, (Object)log$catchup_tx.const__6);
                v6 = log$catchup_tx.__thunk__0__;
                v7 = db;
                v8 = v6.get(v7);
                if (v6 == v8) {
                    log$catchup_tx.__thunk__0__ = log$catchup_tx.__site__0__.fault(v7);
                    v8 = log$catchup_tx.__thunk__0__.get(v7);
                }
                basis = v8;
                d = ((IFn)log$catchup_tx.const__8.getRawRoot()).invoke(data);
                v9 = basis;
                basis = null;
                and__5236__auto__16364 = Numbers.lt((long)((IDatum)d).getT(), (Object)v9);
                v10 = and__5236__auto__16364 != false ? ((IFn)log$catchup_tx.const__10.getRawRoot()).invoke((Object)"datomic.allowLogOverlap") : (and__5236__auto__16364 != false ? Boolean.TRUE : Boolean.FALSE);
                if (v10 == null || v10 == Boolean.FALSE) break block6;
                logger = LoggerFactory.getLogger((String)"datomic.log");
                if (logger.isWarnEnabled()) {
                    v11 = logger;
                    logger = null;
                    v12 = new Object[4];
                    v12[0] = log$catchup_tx.const__12;
                    v12[1] = log$catchup_tx.const__13;
                    v12[2] = log$catchup_tx.const__14;
                    v13 = d;
                    d = null;
                    v12[3] = Numbers.num((long)((IDatum)v13).getT());
                    v11.warn((String)((IFn)log$catchup_tx.const__11.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v12)));
                }
                v14 = new Object[4];
                v14[0] = log$catchup_tx.const__3;
                v15 = db;
                db = null;
                v14[1] = v15;
                v14[2] = log$catchup_tx.const__4;
                v16 = size;
                size = null;
                v14[3] = v16;
                v17 = RT.mapUniqueKeys((Object[])v14);
                break block7;
            }
            v18 = new Object[4];
            v18[0] = log$catchup_tx.const__3;
            v19 = db;
            db = null;
            v18[1] = ((IDbImpl)v19).acceptDataCheck(data, Boolean.FALSE);
            v18[2] = log$catchup_tx.const__4;
            v20 = size;
            size = null;
            v21 = data;
            data = null;
            v22 = v21;
            if (Util.classOf((Object)v21) == log$catchup_tx.__cached_class__0) ** GOTO lbl74
            if (!(v22 instanceof MemorySize)) {
                v22 = v22;
                log$catchup_tx.__cached_class__0 = Util.classOf((Object)v22);
lbl74:
                // 2 sources

                v23 = log$catchup_tx.const__16.getRawRoot().invoke(v22);
            } else {
                v23 = ((MemorySize)v22).memory_size();
            }
            v18[3] = Numbers.add((Object)v20, (Object)v23);
            v17 = RT.mapUniqueKeys((Object[])v18);
        }
        return v17;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$catchup_tx.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"db");
        const__4 = RT.keyword(null, (String)"size");
        const__5 = RT.var((String)"datomic.common", (String)"getx");
        const__6 = RT.keyword(null, (String)"data");
        const__8 = RT.var((String)"clojure.core", (String)"first");
        const__10 = RT.var((String)"datomic.config", (String)"property");
        const__11 = RT.var((String)"datomic.slf4j", (String)"process");
        const__12 = RT.keyword(null, (String)"event");
        const__13 = RT.keyword((String)"log", (String)"overlap");
        const__14 = RT.keyword(null, (String)"t");
        const__16 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
        __thunk__0__ = __site__0__;
    }
}

