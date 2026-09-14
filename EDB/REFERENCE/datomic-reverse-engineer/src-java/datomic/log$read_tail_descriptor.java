/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import org.slf4j.LoggerFactory;

public final class log$read_tail_descriptor
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs) {
        v0 = (IFn)log$read_tail_descriptor.const__0.getRawRoot();
        v1 = cs;
        if (Util.classOf((Object)v1) == log$read_tail_descriptor.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            log$read_tail_descriptor.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = log$read_tail_descriptor.const__1.getRawRoot().invoke(v1, ((IFn)log$read_tail_descriptor.const__2.getRawRoot()).invoke(cs));
        } else {
            v2 = ((ClusteredStore)v1).get_pod(((IFn)log$read_tail_descriptor.const__2.getRawRoot()).invoke(cs));
        }
        v3 = temp__5457__auto__16186 = v0.invoke(v2);
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = temp__5457__auto__16186;
            temp__5457__auto__16186 = null;
            v5 = desc = v4;
            desc = null;
            v6 = cs;
            cs = null;
            desc = ((IFn)log$read_tail_descriptor.const__3.getRawRoot()).invoke(v5, v6);
            v7 = log$read_tail_descriptor.__thunk__0__;
            v8 = desc;
            v9 = v7.get(v8);
            if (v7 == v9) {
                log$read_tail_descriptor.__thunk__0__ = log$read_tail_descriptor.__site__0__.fault(v8);
                v9 = log$read_tail_descriptor.__thunk__0__.get(v8);
            }
            buf = v9;
            v10 = desc;
            desc = null;
            desc = ((IFn)log$read_tail_descriptor.const__5.getRawRoot()).invoke(v10, (Object)log$read_tail_descriptor.const__4);
            logger = LoggerFactory.getLogger((String)"datomic.log");
            if (logger.isDebugEnabled()) {
                v11 = logger;
                logger = null;
                v11.debug((String)((IFn)log$read_tail_descriptor.const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{log$read_tail_descriptor.const__7, log$read_tail_descriptor.const__8, log$read_tail_descriptor.const__9, desc})));
            }
            v12 = ((IFn)log$read_tail_descriptor.const__10.getRawRoot()).invoke(desc);
            if (v12 != null && v12 != Boolean.FALSE) {
            } else {
                ((IFn)log$read_tail_descriptor.const__11.getRawRoot()).invoke((Object)log$read_tail_descriptor.const__12, ((IFn)log$read_tail_descriptor.const__13.getRawRoot()).invoke((Object)"Unable to read log tail ", desc), desc);
            }
            v13 = desc;
            desc = null;
            v14 = buf;
            buf = null;
            v15 = Tuple.create((Object)v13, (Object)v14);
        } else {
            v15 = null;
        }
        return v15;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$read_tail_descriptor.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-pod");
        const__2 = RT.var((String)"datomic.log", (String)"tail-pod-key");
        const__3 = RT.var((String)"datomic.log", (String)"normalize-desc");
        const__4 = RT.keyword(null, (String)"buf");
        const__5 = RT.var((String)"clojure.core", (String)"dissoc");
        const__6 = RT.var((String)"datomic.slf4j", (String)"process");
        const__7 = RT.keyword(null, (String)"event");
        const__8 = RT.keyword((String)"log", (String)"read-tail");
        const__9 = RT.keyword(null, (String)"tail-desc");
        const__10 = RT.var((String)"datomic.log", (String)"tail-descriptor?");
        const__11 = RT.var((String)"datomic.error", (String)"raise");
        const__12 = RT.keyword((String)"db.error", (String)"unable-to-read-log-tail");
        const__13 = RT.var((String)"clojure.core", (String)"str");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__0__ = __site__0__;
    }
}

