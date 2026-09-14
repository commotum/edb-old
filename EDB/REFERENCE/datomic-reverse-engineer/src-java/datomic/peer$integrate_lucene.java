/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.peer$integrate_lucene$fn__21581;
import datomic.peer$integrate_lucene$fn__21584;
import datomic.queue.BlockingConsumer;

public final class peer$integrate_lucene
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Keyword const__2;
    public static final Var const__6;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final AFn const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object db_ref, Object q) {
        v0 = q;
        if (Util.classOf((Object)v0) == peer$integrate_lucene.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof BlockingConsumer)) {
            v0 = v0;
            peer$integrate_lucene.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = peer$integrate_lucene.const__0.getRawRoot().invoke(v0);
        } else {
            v1 = tx = ((BlockingConsumer)v0).take();
        }
        while (true) {
            if (Util.equiv((Object)tx, (Object)peer$integrate_lucene.const__2)) {
                v2 = null;
                break;
            }
            v3 = tx;
            tx = null;
            vec__21578 = ((IFn)new peer$integrate_lucene$fn__21581(q, v3)).invoke();
            txes = RT.nth((Object)vec__21578, (int)RT.intCast((long)0L), null);
            v4 = vec__21578;
            vec__21578 = null;
            ntx = RT.nth((Object)v4, (int)RT.intCast((long)1L), null);
            db = ((IFn)peer$integrate_lucene.const__6.getRawRoot()).invoke(db_ref);
            v5 = peer$integrate_lucene.__thunk__1__;
            v6 = peer$integrate_lucene.__thunk__0__;
            v7 = db;
            v8 = v6.get(v7);
            if (v6 == v8) {
                peer$integrate_lucene.__thunk__0__ = peer$integrate_lucene.__site__0__.fault(v7);
                v8 = peer$integrate_lucene.__thunk__0__.get(v7);
            }
            if (v5 == (v9 = v5.get(v8))) {
                peer$integrate_lucene.__thunk__1__ = peer$integrate_lucene.__site__1__.fault(v8);
                v9 = peer$integrate_lucene.__thunk__1__.get(v8);
            }
            v10 = oft = v9;
            oft = null;
            v11 = db;
            db = null;
            v12 = nft = ((IFn)peer$integrate_lucene.const__9.getRawRoot()).invoke(v10, ((IFn)peer$integrate_lucene.const__10.getRawRoot()).invoke((Object)new peer$integrate_lucene$fn__21584(v11), ((IFn)peer$integrate_lucene.const__11.getRawRoot()).invoke(peer$integrate_lucene.const__12.getRawRoot(), txes)));
            nft = null;
            ((IFn)peer$integrate_lucene.const__13.getRawRoot()).invoke(db_ref, peer$integrate_lucene.const__14.getRawRoot(), (Object)peer$integrate_lucene.const__15, v12);
            v13 = txes;
            txes = null;
            ((IFn)peer$integrate_lucene.const__16.getRawRoot()).invoke((Object)peer$integrate_lucene.const__17, (Object)RT.count((Object)v13));
            v14 = ntx;
            ntx = null;
            if (Util.equiv((Object)v14, (Object)peer$integrate_lucene.const__2)) {
                v2 = null;
                break;
            }
            v15 = q;
            if (Util.classOf((Object)v15) == peer$integrate_lucene.__cached_class__1) ** GOTO lbl55
            if (!(v15 instanceof BlockingConsumer)) {
                v15 = v15;
                peer$integrate_lucene.__cached_class__1 = Util.classOf((Object)v15);
lbl55:
                // 2 sources

                v16 = peer$integrate_lucene.const__0.getRawRoot().invoke(v15);
            } else {
                v16 = ((BlockingConsumer)v15).take();
            }
            tx = v16;
        }
        return v2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$integrate_lucene.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.queue", (String)"take");
        const__2 = RT.keyword(null, (String)"done");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__9 = RT.var((String)"datomic.fulltext-index", (String)"update-fulltext");
        const__10 = RT.var((String)"clojure.core", (String)"filter");
        const__11 = RT.var((String)"clojure.core", (String)"apply");
        const__12 = RT.var((String)"clojure.core", (String)"concat");
        const__13 = RT.var((String)"clojure.core", (String)"swap!");
        const__14 = RT.var((String)"clojure.core", (String)"assoc-in");
        const__15 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"memidx"), (Object)RT.keyword(null, (String)"fulltext"));
        const__16 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__17 = RT.keyword(null, (String)"PeerFulltextBatch");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
        __thunk__1__ = __site__1__;
    }
}

