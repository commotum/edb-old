/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class log$normalize_desc
extends AFunction {
    private static Class __cached_class__0;
    public static final Keyword const__1;
    public static final Object const__2;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Var const__14;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object desc, Object cs) {
        v0 = log$normalize_desc.__thunk__0__;
        v1 = desc;
        v2 = v0.get(v1);
        if (v0 == v2) {
            log$normalize_desc.__thunk__0__ = log$normalize_desc.__site__0__.fault(v1);
            v2 = log$normalize_desc.__thunk__0__.get(v1);
        }
        v3 = or__5238__auto__16184 = v2;
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = or__5238__auto__16184;
            or__5238__auto__16184 = null;
        } else {
            v4 = log$normalize_desc.const__2;
        }
        G__16182 = rev = RT.longCast((Object)v4);
        switch ((int)G__16182) {
            case 1: {
                if (1L != G__16182) ** GOTO lbl54
                v5 = (IFn)log$normalize_desc.const__4.getRawRoot();
                v6 = desc;
                desc = null;
                v7 = log$normalize_desc.__thunk__1__;
                v8 = (IFn)log$normalize_desc.const__7.getRawRoot();
                v9 = cs;
                if (Util.classOf((Object)v9) == log$normalize_desc.__cached_class__0) ** GOTO lbl28
                if (!(v9 instanceof ClusteredStore)) {
                    v9 = v9;
                    log$normalize_desc.__cached_class__0 = Util.classOf((Object)v9);
lbl28:
                    // 2 sources

                    v10 = cs;
                    cs = null;
                    v11 = log$normalize_desc.const__8.getRawRoot().invoke(v9, ((IFn)log$normalize_desc.const__9.getRawRoot()).invoke(v10));
                } else {
                    v12 = cs;
                    cs = null;
                    v11 = ((ClusteredStore)v9).get_ref(((IFn)log$normalize_desc.const__9.getRawRoot()).invoke(v12));
                }
                v13 = v8.invoke(v11);
                v14 = v7.get(v13);
                if (v7 == v14) {
                    log$normalize_desc.__thunk__1__ = log$normalize_desc.__site__1__.fault(v13);
                    v14 = log$normalize_desc.__thunk__1__.get(v13);
                }
                v15 = v5.invoke(v6, (Object)log$normalize_desc.const__1, log$normalize_desc.const__2, (Object)log$normalize_desc.const__5, v14);
                break;
            }
            case 2: {
                if (2L == G__16182) {
                    v15 = desc;
                    desc = null;
                    break;
                }
                ** GOTO lbl54
            }
            case 3: {
                if (3L == G__16182) {
                    v15 = desc;
                    desc = null;
                    break;
                }
            }
lbl54:
            // 5 sources

            default: {
                v15 = ((IFn)log$normalize_desc.const__12.getRawRoot()).invoke((Object)log$normalize_desc.const__13, ((IFn)log$normalize_desc.const__14.getRawRoot()).invoke((Object)"This version of Datomic cannot read log version ", (Object)Numbers.num((long)rev)), (Object)RT.mapUniqueKeys((Object[])new Object[]{log$normalize_desc.const__1, Numbers.num((long)rev)}));
            }
        }
        return v15;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$normalize_desc.invokeStatic(object3, object4);
    }

    static {
        const__1 = RT.keyword((String)"d", (String)"l");
        const__2 = 1L;
        const__4 = RT.var((String)"clojure.core", (String)"assoc");
        const__5 = RT.keyword((String)"d", (String)"r");
        const__7 = RT.var((String)"clojure.core", (String)"deref");
        const__8 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__9 = RT.var((String)"datomic.log", (String)"legacy-root-ref-key");
        const__12 = RT.var((String)"datomic.error", (String)"state");
        const__13 = RT.keyword((String)"db.error", (String)"log-version");
        const__14 = RT.var((String)"clojure.core", (String)"str");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"l"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__1__ = __site__1__;
    }
}

