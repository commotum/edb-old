/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.simple_kv.KV;

public final class simple_kv$get_with_retry
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    public static final Var const__0;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object skv, Object k) {
        block11: {
            block18: {
                block17: {
                    block16: {
                        block15: {
                            block14: {
                                block13: {
                                    block12: {
                                        block10: {
                                            v0 = skv;
                                            if (Util.classOf((Object)v0) == simple_kv$get_with_retry.__cached_class__0) ** GOTO lbl6
                                            if (!(v0 instanceof KV)) {
                                                v0 = v0;
                                                simple_kv$get_with_retry.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
                                                // 2 sources

                                                v1 = simple_kv$get_with_retry.const__0.getRawRoot().invoke(v0, k);
                                            } else {
                                                v1 = ((KV)v0).get(k);
                                            }
                                            v2 = or__5238__auto__16738 = v1;
                                            if (v2 == null || v2 == Boolean.FALSE) break block10;
                                            v3 = or__5238__auto__16738;
                                            or__5238__auto__16738 = null;
                                            break block11;
                                        }
                                        Thread.sleep(10L);
                                        v4 = or__5238__auto__16737 = null;
                                        if (v4 == null || v4 == Boolean.FALSE) break block12;
                                        v3 = or__5238__auto__16737;
                                        or__5238__auto__16737 = null;
                                        break block11;
                                    }
                                    v5 = skv;
                                    if (Util.classOf((Object)v5) == simple_kv$get_with_retry.__cached_class__1) ** GOTO lbl27
                                    if (!(v5 instanceof KV)) {
                                        v5 = v5;
                                        simple_kv$get_with_retry.__cached_class__1 = Util.classOf((Object)v5);
lbl27:
                                        // 2 sources

                                        v6 = simple_kv$get_with_retry.const__0.getRawRoot().invoke(v5, k);
                                    } else {
                                        v6 = ((KV)v5).get(k);
                                    }
                                    v7 = or__5238__auto__16736 = v6;
                                    if (v7 == null || v7 == Boolean.FALSE) break block13;
                                    v3 = or__5238__auto__16736;
                                    or__5238__auto__16736 = null;
                                    break block11;
                                }
                                Thread.sleep(40L);
                                v8 = or__5238__auto__16735 = null;
                                if (v8 == null || v8 == Boolean.FALSE) break block14;
                                v3 = or__5238__auto__16735;
                                or__5238__auto__16735 = null;
                                break block11;
                            }
                            v9 = skv;
                            if (Util.classOf((Object)v9) == simple_kv$get_with_retry.__cached_class__2) ** GOTO lbl48
                            if (!(v9 instanceof KV)) {
                                v9 = v9;
                                simple_kv$get_with_retry.__cached_class__2 = Util.classOf((Object)v9);
lbl48:
                                // 2 sources

                                v10 = simple_kv$get_with_retry.const__0.getRawRoot().invoke(v9, k);
                            } else {
                                v10 = ((KV)v9).get(k);
                            }
                            v11 = or__5238__auto__16734 = v10;
                            if (v11 == null || v11 == Boolean.FALSE) break block15;
                            v3 = or__5238__auto__16734;
                            or__5238__auto__16734 = null;
                            break block11;
                        }
                        Thread.sleep(160L);
                        v12 = or__5238__auto__16733 = null;
                        if (v12 == null || v12 == Boolean.FALSE) break block16;
                        v3 = or__5238__auto__16733;
                        or__5238__auto__16733 = null;
                        break block11;
                    }
                    v13 = skv;
                    if (Util.classOf((Object)v13) == simple_kv$get_with_retry.__cached_class__3) ** GOTO lbl69
                    if (!(v13 instanceof KV)) {
                        v13 = v13;
                        simple_kv$get_with_retry.__cached_class__3 = Util.classOf((Object)v13);
lbl69:
                        // 2 sources

                        v14 = simple_kv$get_with_retry.const__0.getRawRoot().invoke(v13, k);
                    } else {
                        v14 = ((KV)v13).get(k);
                    }
                    v15 = or__5238__auto__16732 = v14;
                    if (v15 == null || v15 == Boolean.FALSE) break block17;
                    v3 = or__5238__auto__16732;
                    or__5238__auto__16732 = null;
                    break block11;
                }
                Thread.sleep(480L);
                v16 = or__5238__auto__16731 = null;
                if (v16 == null || v16 == Boolean.FALSE) break block18;
                v3 = or__5238__auto__16731;
                or__5238__auto__16731 = null;
                break block11;
            }
            v17 = skv;
            skv = null;
            v18 = v17;
            if (Util.classOf((Object)v17) == simple_kv$get_with_retry.__cached_class__4) ** GOTO lbl92
            if (!(v18 instanceof KV)) {
                v18 = v18;
                simple_kv$get_with_retry.__cached_class__4 = Util.classOf((Object)v18);
lbl92:
                // 2 sources

                v19 = k;
                k = null;
                v3 = simple_kv$get_with_retry.const__0.getRawRoot().invoke(v18, v19);
            } else {
                v20 = k;
                k = null;
                v3 = ((KV)v18).get(v20);
            }
        }
        return v3;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return simple_kv$get_with_retry.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.simple-kv", (String)"get");
    }
}

