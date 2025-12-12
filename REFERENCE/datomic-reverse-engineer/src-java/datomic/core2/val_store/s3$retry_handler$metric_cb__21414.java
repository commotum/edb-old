/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;

public final class s3$retry_handler$metric_cb__21414
extends AFunction {
    Object op;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__13;
    public static final Var const__14;
    public static final Keyword const__15;
    public static final Keyword const__16;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final Keyword const__23;

    public s3$retry_handler$metric_cb__21414(Object object) {
        this.op = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object p__21413) {
        block14: {
            block12: {
                block13: {
                    v0 = p__21413;
                    p__21413 = null;
                    map__21415 = v0;
                    v1 = ((IFn)s3$retry_handler$metric_cb__21414.const__0.getRawRoot()).invoke(map__21415);
                    if (v1 != null && v1 != Boolean.FALSE) {
                        v2 = ((IFn)s3$retry_handler$metric_cb__21414.const__1.getRawRoot()).invoke(map__21415);
                        if (v2 != null && v2 != Boolean.FALSE) {
                            v3 = map__21415;
                            map__21415 = null;
                            v4 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)s3$retry_handler$metric_cb__21414.const__2.getRawRoot()).invoke(v3)));
                        } else {
                            v5 = ((IFn)s3$retry_handler$metric_cb__21414.const__3.getRawRoot()).invoke(map__21415);
                            if (v5 != null && v5 != Boolean.FALSE) {
                                v6 = map__21415;
                                map__21415 = null;
                                v4 = ((IFn)s3$retry_handler$metric_cb__21414.const__4.getRawRoot()).invoke(v6);
                            } else {
                                v4 = PersistentArrayMap.EMPTY;
                            }
                        }
                    } else {
                        v4 = map__21415;
                        map__21415 = null;
                    }
                    map__21415 = v4;
                    ok_QMARK_ = RT.get((Object)map__21415, (Object)s3$retry_handler$metric_cb__21414.const__6);
                    i = RT.get((Object)map__21415, (Object)s3$retry_handler$metric_cb__21414.const__7);
                    v7 = map__21415;
                    map__21415 = null;
                    result = RT.get((Object)v7, (Object)s3$retry_handler$metric_cb__21414.const__8);
                    v8 = ok_QMARK_;
                    metric_name = RT.get((Object)(v8 != null && v8 != Boolean.FALSE ? s3$retry_handler$metric_cb__21414.const__9.getRawRoot() : s3$retry_handler$metric_cb__21414.const__10.getRawRoot()), (Object)this.op);
                    v9 = ok_QMARK_;
                    ok_QMARK_ = null;
                    if (v9 == null || v9 == Boolean.FALSE) break block12;
                    if (RT.longCast((Object)((Number)i)) <= 1L) break block13;
                    v10 = s3$retry_handler$metric_cb__21414.const__14.getRawRoot();
                    if (Util.classOf((Object)v10) == s3$retry_handler$metric_cb__21414.__cached_class__0) ** GOTO lbl40
                    if (!(v10 instanceof Impl)) {
                        v10 = v10;
                        s3$retry_handler$metric_cb__21414.__cached_class__0 = Util.classOf((Object)v10);
lbl40:
                        // 2 sources

                        v11 = new Object[6];
                        v11[0] = s3$retry_handler$metric_cb__21414.const__15;
                        v12 = metric_name;
                        metric_name = null;
                        v11[1] = v12;
                        v11[2] = s3$retry_handler$metric_cb__21414.const__16;
                        v13 = i;
                        i = null;
                        v11[3] = v13;
                        v11[4] = s3$retry_handler$metric_cb__21414.const__17;
                        v11[5] = s3$retry_handler$metric_cb__21414.const__18;
                        this = null;
                        v14 = s3$retry_handler$metric_cb__21414.const__13.getRawRoot().invoke(v10, (Object)RT.mapUniqueKeys((Object[])v11));
                    } else {
                        v15 = new Object[6];
                        v15[0] = s3$retry_handler$metric_cb__21414.const__15;
                        v16 = metric_name;
                        metric_name = null;
                        v15[1] = v16;
                        v15[2] = s3$retry_handler$metric_cb__21414.const__16;
                        v17 = i;
                        i = null;
                        v15[3] = v17;
                        v15[4] = s3$retry_handler$metric_cb__21414.const__17;
                        v15[5] = s3$retry_handler$metric_cb__21414.const__18;
                        v14 = ((Impl)v10).metric_STAR_((Object)RT.mapUniqueKeys((Object[])v15));
                    }
                    break block14;
                }
                v14 = null;
                break block14;
            }
            v18 = s3$retry_handler$metric_cb__21414.const__14.getRawRoot();
            if (Util.classOf((Object)v18) == s3$retry_handler$metric_cb__21414.__cached_class__1) ** GOTO lbl76
            if (!(v18 instanceof Impl)) {
                v18 = v18;
                s3$retry_handler$metric_cb__21414.__cached_class__1 = Util.classOf((Object)v18);
lbl76:
                // 2 sources

                v19 = new Object[6];
                v19[0] = s3$retry_handler$metric_cb__21414.const__15;
                v20 = metric_name;
                metric_name = null;
                v19[1] = v20;
                v19[2] = s3$retry_handler$metric_cb__21414.const__16;
                v19[3] = i;
                v19[4] = s3$retry_handler$metric_cb__21414.const__17;
                v19[5] = s3$retry_handler$metric_cb__21414.const__18;
                v21 = s3$retry_handler$metric_cb__21414.const__13.getRawRoot().invoke(v18, (Object)RT.mapUniqueKeys((Object[])v19));
            } else {
                v22 = new Object[6];
                v22[0] = s3$retry_handler$metric_cb__21414.const__15;
                v23 = metric_name;
                metric_name = null;
                v22[1] = v23;
                v22[2] = s3$retry_handler$metric_cb__21414.const__16;
                v22[3] = i;
                v22[4] = s3$retry_handler$metric_cb__21414.const__17;
                v22[5] = s3$retry_handler$metric_cb__21414.const__18;
                v21 = ((Impl)v18).metric_STAR_((Object)RT.mapUniqueKeys((Object[])v22));
            }
            if (Util.classOf((Object)(v24 = s3$retry_handler$metric_cb__21414.const__14.getRawRoot())) == s3$retry_handler$metric_cb__21414.__cached_class__2) ** GOTO lbl101
            if (!(v24 instanceof Impl)) {
                v24 = v24;
                s3$retry_handler$metric_cb__21414.__cached_class__2 = Util.classOf((Object)v24);
lbl101:
                // 2 sources

                v25 = result;
                result = null;
                v26 = new Object[6];
                v26[0] = s3$retry_handler$metric_cb__21414.const__21;
                v26[1] = "S3 op failed";
                v26[2] = s3$retry_handler$metric_cb__21414.const__22;
                v26[3] = this.op;
                v26[4] = s3$retry_handler$metric_cb__21414.const__23;
                v27 = i;
                i = null;
                v26[5] = v27;
                this = null;
                v14 = s3$retry_handler$metric_cb__21414.const__19.getRawRoot().invoke(v24, ((IFn)s3$retry_handler$metric_cb__21414.const__20.getRawRoot()).invoke(v25, (Object)RT.mapUniqueKeys((Object[])v26)));
            } else {
                v28 = result;
                result = null;
                v29 = new Object[6];
                v29[0] = s3$retry_handler$metric_cb__21414.const__21;
                v29[1] = "S3 op failed";
                v29[2] = s3$retry_handler$metric_cb__21414.const__22;
                v29[3] = this.op;
                v29[4] = s3$retry_handler$metric_cb__21414.const__23;
                v30 = i;
                i = null;
                v29[5] = v30;
                v14 = ((Impl)v24).event_STAR_(((IFn)s3$retry_handler$metric_cb__21414.const__20.getRawRoot()).invoke(v28, (Object)RT.mapUniqueKeys((Object[])v29)));
            }
        }
        return v14;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"next");
        const__2 = RT.var((String)"clojure.core", (String)"to-array");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.var((String)"clojure.core", (String)"first");
        const__6 = RT.keyword(null, (String)"ok?");
        const__7 = RT.keyword(null, (String)"i");
        const__8 = RT.keyword(null, (String)"result");
        const__9 = RT.var((String)"datomic.core2.val-store.s3", (String)"retry-success-metrics");
        const__10 = RT.var((String)"datomic.core2.val-store.s3", (String)"retry-failure-metrics");
        const__13 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__14 = RT.var((String)"cognitect.caster", (String)"instance");
        const__15 = RT.keyword(null, (String)"name");
        const__16 = RT.keyword(null, (String)"value");
        const__17 = RT.keyword(null, (String)"units");
        const__18 = RT.keyword(null, (String)"count");
        const__19 = RT.var((String)"cognitect.caster", (String)"event*");
        const__20 = RT.var((String)"clojure.core", (String)"merge");
        const__21 = RT.keyword(null, (String)"msg");
        const__22 = RT.keyword((String)"datomic.core2.val-store.s3", (String)"op");
        const__23 = RT.keyword((String)"datomic.core2.val-store.s3", (String)"retry");
    }
}

