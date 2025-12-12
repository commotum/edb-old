/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LD
 *  clojure.lang.IFn$OLO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;

public final class fs$wrap_metric_handler$fn__21259
extends AFunction {
    Object op;
    Object f;
    long start_nsec;
    Object k;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Object const__11;
    public static final Object const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Keyword const__24;
    public static final Keyword const__25;

    public fs$wrap_metric_handler$fn__21259(Object object, Object object2, long l, Object object3) {
        this.op = object;
        this.f = object2;
        this.start_nsec = l;
        this.k = object3;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        block7: {
            block6: {
                result = ((IFn)this.f).invoke(this.k);
                ok_QMARK_ = ((IFn)fs$wrap_metric_handler$fn__21259.const__0.getRawRoot()).invoke(result);
                nsec = Numbers.minus((long)System.nanoTime(), (long)this.start_nsec);
                if (!Util.equiv((Object)this.op, (Object)fs$wrap_metric_handler$fn__21259.const__3)) break block6;
                ((IFn.OLO)fs$wrap_metric_handler$fn__21259.const__4.getRawRoot()).invokePrim((Object)fs$wrap_metric_handler$fn__21259.const__5, nsec);
                v0 = fs$wrap_metric_handler$fn__21259.const__7.getRawRoot();
                if (Util.classOf((Object)v0) == fs$wrap_metric_handler$fn__21259.__cached_class__0) ** GOTO lbl12
                if (!(v0 instanceof Impl)) {
                    v0 = v0;
                    fs$wrap_metric_handler$fn__21259.__cached_class__0 = Util.classOf((Object)v0);
lbl12:
                    // 2 sources

                    v1 = new Object[6];
                    v1[0] = fs$wrap_metric_handler$fn__21259.const__8;
                    v1[1] = fs$wrap_metric_handler$fn__21259.const__9;
                    v1[2] = fs$wrap_metric_handler$fn__21259.const__10;
                    v2 = ok_QMARK_;
                    v1[3] = v2 != null && v2 != Boolean.FALSE ? fs$wrap_metric_handler$fn__21259.const__11 : fs$wrap_metric_handler$fn__21259.const__12;
                    v1[4] = fs$wrap_metric_handler$fn__21259.const__13;
                    v1[5] = fs$wrap_metric_handler$fn__21259.const__14;
                    v3 = fs$wrap_metric_handler$fn__21259.const__6.getRawRoot().invoke(v0, (Object)RT.mapUniqueKeys((Object[])v1));
                } else {
                    v4 = new Object[6];
                    v4[0] = fs$wrap_metric_handler$fn__21259.const__8;
                    v4[1] = fs$wrap_metric_handler$fn__21259.const__9;
                    v4[2] = fs$wrap_metric_handler$fn__21259.const__10;
                    v5 = ok_QMARK_;
                    v4[3] = v5 != null && v5 != Boolean.FALSE ? fs$wrap_metric_handler$fn__21259.const__11 : fs$wrap_metric_handler$fn__21259.const__12;
                    v4[4] = fs$wrap_metric_handler$fn__21259.const__13;
                    v4[5] = fs$wrap_metric_handler$fn__21259.const__14;
                    v3 = ((Impl)v0).metric_STAR_((Object)RT.mapUniqueKeys((Object[])v4));
                }
            }
            if (Util.classOf((Object)(v6 = fs$wrap_metric_handler$fn__21259.const__7.getRawRoot())) == fs$wrap_metric_handler$fn__21259.__cached_class__1) ** GOTO lbl36
            if (!(v6 instanceof Impl)) {
                v6 = v6;
                fs$wrap_metric_handler$fn__21259.__cached_class__1 = Util.classOf((Object)v6);
lbl36:
                // 2 sources

                v7 = new Object[8];
                v7[0] = fs$wrap_metric_handler$fn__21259.const__8;
                v8 = ok_QMARK_;
                ok_QMARK_ = null;
                v7[1] = RT.get((Object)(v8 != null && v8 != Boolean.FALSE ? fs$wrap_metric_handler$fn__21259.const__16.getRawRoot() : fs$wrap_metric_handler$fn__21259.const__17.getRawRoot()), (Object)this.op);
                v7[2] = fs$wrap_metric_handler$fn__21259.const__10;
                v7[3] = ((IFn.LD)fs$wrap_metric_handler$fn__21259.const__18.getRawRoot()).invokePrim(nsec);
                v7[4] = fs$wrap_metric_handler$fn__21259.const__13;
                v7[5] = fs$wrap_metric_handler$fn__21259.const__19;
                v7[6] = fs$wrap_metric_handler$fn__21259.const__20;
                v7[7] = this.k;
                v9 = fs$wrap_metric_handler$fn__21259.const__6.getRawRoot().invoke(v6, (Object)RT.mapUniqueKeys((Object[])v7));
            } else {
                v10 = new Object[8];
                v10[0] = fs$wrap_metric_handler$fn__21259.const__8;
                v11 = ok_QMARK_;
                ok_QMARK_ = null;
                v10[1] = RT.get((Object)(v11 != null && v11 != Boolean.FALSE ? fs$wrap_metric_handler$fn__21259.const__16.getRawRoot() : fs$wrap_metric_handler$fn__21259.const__17.getRawRoot()), (Object)this.op);
                v10[2] = fs$wrap_metric_handler$fn__21259.const__10;
                v10[3] = ((IFn.LD)fs$wrap_metric_handler$fn__21259.const__18.getRawRoot()).invokePrim(nsec);
                v10[4] = fs$wrap_metric_handler$fn__21259.const__13;
                v10[5] = fs$wrap_metric_handler$fn__21259.const__19;
                v10[6] = fs$wrap_metric_handler$fn__21259.const__20;
                v10[7] = this.k;
                v9 = ((Impl)v6).metric_STAR_((Object)RT.mapUniqueKeys((Object[])v10));
            }
            v12 = ((IFn)fs$wrap_metric_handler$fn__21259.const__21.getRawRoot()).invoke(result);
            if (v12 == null || v12 == Boolean.FALSE) break block7;
            v13 = fs$wrap_metric_handler$fn__21259.const__7.getRawRoot();
            if (Util.classOf((Object)v13) == fs$wrap_metric_handler$fn__21259.__cached_class__2) ** GOTO lbl68
            if (!(v13 instanceof Impl)) {
                v13 = v13;
                fs$wrap_metric_handler$fn__21259.__cached_class__2 = Util.classOf((Object)v13);
lbl68:
                // 2 sources

                v14 = fs$wrap_metric_handler$fn__21259.const__22.getRawRoot().invoke(v13, ((IFn)fs$wrap_metric_handler$fn__21259.const__23.getRawRoot()).invoke(result, (Object)fs$wrap_metric_handler$fn__21259.const__24, (Object)"FS cache op failed", (Object)fs$wrap_metric_handler$fn__21259.const__25, this.k));
            } else {
                v14 = ((Impl)v13).event_STAR_(((IFn)fs$wrap_metric_handler$fn__21259.const__23.getRawRoot()).invoke(result, (Object)fs$wrap_metric_handler$fn__21259.const__24, (Object)"FS cache op failed", (Object)fs$wrap_metric_handler$fn__21259.const__25, this.k));
            }
        }
        var1_1 = null;
        return result;
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store.spi", (String)"val-op-succeeded?");
        const__3 = RT.keyword(null, (String)"get");
        const__4 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__5 = RT.keyword(null, (String)"fs-ns");
        const__6 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__7 = RT.var((String)"cognitect.caster", (String)"instance");
        const__8 = RT.keyword(null, (String)"name");
        const__9 = RT.keyword(null, (String)"efs.hits");
        const__10 = RT.keyword(null, (String)"value");
        const__11 = 1L;
        const__12 = 0L;
        const__13 = RT.keyword(null, (String)"units");
        const__14 = RT.keyword(null, (String)"count");
        const__16 = RT.var((String)"datomic.core2.val-store.fs", (String)"success-metrics");
        const__17 = RT.var((String)"datomic.core2.val-store.fs", (String)"failure-metrics");
        const__18 = RT.var((String)"datomic.measure.io-stats", (String)"ns->ms");
        const__19 = RT.keyword(null, (String)"msec");
        const__20 = RT.keyword((String)"datomic.core2.val-store.fs", (String)"key");
        const__21 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
        const__22 = RT.var((String)"cognitect.caster", (String)"event*");
        const__23 = RT.var((String)"clojure.core", (String)"assoc");
        const__24 = RT.keyword(null, (String)"msg");
        const__25 = RT.keyword((String)"datomic.core2.val-store.fs", (String)"k");
    }
}

