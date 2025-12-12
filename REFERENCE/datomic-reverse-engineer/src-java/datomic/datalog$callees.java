/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.data.EqualityPartition
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.data.EqualityPartition;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$callees
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Var const__14;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object x) {
        block13: {
            block12: {
                v0 = ((IFn)datalog$callees.const__0.getRawRoot()).invoke(x);
                if (v0 == null || v0 == Boolean.FALSE) break block12;
                v1 = ((IFn)datalog$callees.const__2.getRawRoot()).invoke(x);
                v2 = x;
                x = null;
                v3 = ((IFn)datalog$callees.const__1.getRawRoot()).invoke(v1, ((IFn)datalog$callees.const__3.getRawRoot()).invoke(datalog$callees.const__4.getRawRoot(), ((IFn)datalog$callees.const__5.getRawRoot()).invoke(v2)));
                break block13;
            }
            v4 = x;
            if (Util.classOf((Object)v4) == datalog$callees.__cached_class__0) ** GOTO lbl14
            if (!(v4 instanceof EqualityPartition)) {
                v4 = v4;
                datalog$callees.__cached_class__0 = Util.classOf((Object)v4);
lbl14:
                // 2 sources

                v5 = datalog$callees.const__6.getRawRoot().invoke(v4);
            } else {
                v5 = ((EqualityPartition)v4).equality_partition();
            }
            G__18732 = v5;
            switch (Util.hash((Object)G__18732) >> 2 & 7) {
                case 0: {
                    if (G__18732 == datalog$callees.const__7) {
                        v6 = x;
                        x = null;
                        v3 = ((IFn)datalog$callees.const__3.getRawRoot()).invoke(datalog$callees.const__4.getRawRoot(), v6);
                        break;
                    }
                    ** GOTO lbl45
                }
                case 1: {
                    if (G__18732 == datalog$callees.const__8) {
                        v7 = ((IFn)datalog$callees.const__3.getRawRoot()).invoke(((IFn)datalog$callees.const__4.getRawRoot()).invoke(((IFn)datalog$callees.const__10.getRawRoot()).invoke(x)));
                        v8 = x;
                        x = null;
                        v3 = ((IFn)datalog$callees.const__9.getRawRoot()).invoke(v7, ((IFn)datalog$callees.const__3.getRawRoot()).invoke(((IFn)datalog$callees.const__4.getRawRoot()).invoke(((IFn)datalog$callees.const__11.getRawRoot()).invoke(v8))));
                        break;
                    }
                    ** GOTO lbl45
                }
                case 3: {
                    if (G__18732 == datalog$callees.const__12) {
                        v9 = x;
                        x = null;
                        v3 = ((IFn)datalog$callees.const__3.getRawRoot()).invoke(datalog$callees.const__4.getRawRoot(), v9);
                        break;
                    }
                    ** GOTO lbl45
                }
                case 5: {
                    if (G__18732 == datalog$callees.const__13) {
                        v3 = null;
                        break;
                    }
                }
lbl45:
                // 6 sources

                default: {
                    v10 = G__18732;
                    G__18732 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)datalog$callees.const__14.getRawRoot()).invoke((Object)"No matching clause: ", v10));
                }
            }
        }
        return v3;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$callees.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"list?");
        const__1 = RT.var((String)"clojure.core", (String)"cons");
        const__2 = RT.var((String)"clojure.core", (String)"first");
        const__3 = RT.var((String)"clojure.core", (String)"mapcat");
        const__4 = RT.var((String)"datomic.datalog", (String)"callees");
        const__5 = RT.var((String)"clojure.core", (String)"rest");
        const__6 = RT.var((String)"clojure.data", (String)"equality-partition");
        const__7 = RT.keyword(null, (String)"sequential");
        const__8 = RT.keyword(null, (String)"map");
        const__9 = RT.var((String)"clojure.core", (String)"concat");
        const__10 = RT.var((String)"clojure.core", (String)"keys");
        const__11 = RT.var((String)"clojure.core", (String)"vals");
        const__12 = RT.keyword(null, (String)"set");
        const__13 = RT.keyword(null, (String)"atom");
        const__14 = RT.var((String)"clojure.core", (String)"str");
    }
}

