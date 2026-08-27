/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;

public final class connector$stop_all_connectors
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__14;
    public static final Var const__15;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic() {
        seq_21143 = ((IFn)connector$stop_all_connectors.const__0.getRawRoot()).invoke(((IFn)connector$stop_all_connectors.const__1.getRawRoot()).invoke(connector$stop_all_connectors.const__2.getRawRoot()));
        chunk_21144 = null;
        count_21145 = 0L;
        i_21146 = 0L;
        while (true) {
            block8: {
                block6: {
                    block7: {
                        if (i_21146 >= count_21145) break block6;
                        v0 = k = ((Indexed)chunk_21144).nth(RT.intCast((long)i_21146));
                        k = null;
                        v1 = temp__5457__auto__21148 = ((IFn)connector$stop_all_connectors.const__5.getRawRoot()).invoke(connector$stop_all_connectors.const__2.getRawRoot(), v0);
                        if (v1 == null || v1 == Boolean.FALSE) break block7;
                        v2 = temp__5457__auto__21148;
                        temp__5457__auto__21148 = null;
                        sfb = v2;
                        v3 = (IFn)connector$stop_all_connectors.const__6.getRawRoot();
                        v4 = sfb;
                        sfb = null;
                        v5 = v4;
                        if (Util.classOf((Object)v4) == connector$stop_all_connectors.__cached_class__0) ** GOTO lbl22
                        if (!(v5 instanceof AsyncShutdown)) {
                            v5 = v5;
                            connector$stop_all_connectors.__cached_class__0 = Util.classOf((Object)v5);
lbl22:
                            // 2 sources

                            v6 = connector$stop_all_connectors.const__7.getRawRoot().invoke(v5);
                        } else {
                            v6 = ((AsyncShutdown)v5).async_shutdown();
                        }
                        v3.invoke(v6);
                    }
                    v7 = seq_21143;
                    seq_21143 = null;
                    v8 = chunk_21144;
                    chunk_21144 = null;
                    ++i_21146;
                    chunk_21144 = v8;
                    seq_21143 = v7;
                    continue;
                }
                v9 = seq_21143;
                seq_21143 = null;
                v10 = temp__5457__auto__21151 = ((IFn)connector$stop_all_connectors.const__0.getRawRoot()).invoke(v9);
                if (v10 == null || v10 == Boolean.FALSE) break;
                v11 = temp__5457__auto__21151;
                temp__5457__auto__21151 = null;
                seq_21143 = v11;
                v12 = ((IFn)connector$stop_all_connectors.const__9.getRawRoot()).invoke(seq_21143);
                if (v12 != null && v12 != Boolean.FALSE) {
                    c__5719__auto__21149 = ((IFn)connector$stop_all_connectors.const__10.getRawRoot()).invoke(seq_21143);
                    v13 = seq_21143;
                    seq_21143 = null;
                    v14 = c__5719__auto__21149;
                    v15 = c__5719__auto__21149;
                    c__5719__auto__21149 = null;
                    i_21146 = RT.intCast((long)0L);
                    count_21145 = RT.intCast((int)RT.count((Object)v15));
                    chunk_21144 = v14;
                    seq_21143 = ((IFn)connector$stop_all_connectors.const__11.getRawRoot()).invoke(v13);
                    continue;
                }
                v16 = k = ((IFn)connector$stop_all_connectors.const__14.getRawRoot()).invoke(seq_21143);
                k = null;
                v17 = temp__5457__auto__21150 = ((IFn)connector$stop_all_connectors.const__5.getRawRoot()).invoke(connector$stop_all_connectors.const__2.getRawRoot(), v16);
                if (v17 == null || v17 == Boolean.FALSE) break block8;
                v18 = temp__5457__auto__21150;
                temp__5457__auto__21150 = null;
                sfb = v18;
                v19 = (IFn)connector$stop_all_connectors.const__6.getRawRoot();
                v20 = sfb;
                sfb = null;
                v21 = v20;
                if (Util.classOf((Object)v20) == connector$stop_all_connectors.__cached_class__1) ** GOTO lbl72
                if (!(v21 instanceof AsyncShutdown)) {
                    v21 = v21;
                    connector$stop_all_connectors.__cached_class__1 = Util.classOf((Object)v21);
lbl72:
                    // 2 sources

                    v22 = connector$stop_all_connectors.const__7.getRawRoot().invoke(v21);
                } else {
                    v22 = ((AsyncShutdown)v21).async_shutdown();
                }
                v19.invoke(v22);
            }
            v23 = seq_21143;
            seq_21143 = null;
            i_21146 = 0L;
            count_21145 = 0L;
            chunk_21144 = null;
            seq_21143 = ((IFn)connector$stop_all_connectors.const__15.getRawRoot()).invoke(v23);
        }
        return null;
    }

    public Object invoke() {
        return connector$stop_all_connectors.invokeStatic();
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq");
        const__1 = RT.var((String)"datomic.cache", (String)"cache-keys");
        const__2 = RT.var((String)"datomic.connector", (String)"sfb-cache");
        const__5 = RT.var((String)"datomic.cache", (String)"remove");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__7 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__14 = RT.var((String)"clojure.core", (String)"first");
        const__15 = RT.var((String)"clojure.core", (String)"next");
    }
}

