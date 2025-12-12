/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.aws;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ddb$de_attribute_value
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");
    public static final AFn const__2 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"BOOL")});
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"N")});
    public static final AFn const__6 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"B")});
    public static final AFn const__8 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"S")});
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"ex-info");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"BOOL"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"N"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"B"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"S"));
    static ILookupThunk __thunk__3__ = __site__3__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object m) {
        G__20435 = ((IFn)ddb$de_attribute_value.const__0.getRawRoot()).invoke(((IFn)ddb$de_attribute_value.const__1.getRawRoot()).invoke(m));
        switch (Util.hash((Object)G__20435) >> 0 & 7) {
            case 2: {
                if (Util.equiv((Object)G__20435, (Object)ddb$de_attribute_value.const__2)) {
                    v0 = ddb$de_attribute_value.__thunk__0__;
                    v1 = m;
                    m = null;
                    v2 = v0.get(v1);
                    if (v0 != v2) break;
                    ddb$de_attribute_value.__thunk__0__ = ddb$de_attribute_value.__site__0__.fault(v1);
                    v2 = ddb$de_attribute_value.__thunk__0__.get(v1);
                    break;
                }
                ** GOTO lbl51
            }
            case 3: {
                if (Util.equiv((Object)G__20435, (Object)ddb$de_attribute_value.const__4)) {
                    v3 = ddb$de_attribute_value.__thunk__1__;
                    v4 = m;
                    m = null;
                    v5 = v3.get(v4);
                    if (v3 == v5) {
                        ddb$de_attribute_value.__thunk__1__ = ddb$de_attribute_value.__site__1__.fault(v4);
                        v5 = ddb$de_attribute_value.__thunk__1__.get(v4);
                    }
                    v2 = Numbers.num((long)Long.parseLong((String)v5));
                    break;
                }
                ** GOTO lbl51
            }
            case 4: {
                if (Util.equiv((Object)G__20435, (Object)ddb$de_attribute_value.const__6)) {
                    v6 = ddb$de_attribute_value.__thunk__2__;
                    v7 = m;
                    m = null;
                    v2 = v6.get(v7);
                    if (v6 != v2) break;
                    ddb$de_attribute_value.__thunk__2__ = ddb$de_attribute_value.__site__2__.fault(v7);
                    v2 = ddb$de_attribute_value.__thunk__2__.get(v7);
                    break;
                }
                ** GOTO lbl51
            }
            case 7: {
                if (Util.equiv((Object)G__20435, (Object)ddb$de_attribute_value.const__8)) {
                    v8 = ddb$de_attribute_value.__thunk__3__;
                    v9 = m;
                    m = null;
                    v2 = v8.get(v9);
                    if (v8 != v2) break;
                    ddb$de_attribute_value.__thunk__3__ = ddb$de_attribute_value.__site__3__.fault(v9);
                    v2 = ddb$de_attribute_value.__thunk__3__.get(v9);
                    break;
                }
            }
lbl51:
            // 6 sources

            default: {
                v10 = m;
                m = null;
                throw (Throwable)((IFn)ddb$de_attribute_value.const__10.getRawRoot()).invoke((Object)"Could not parse DDB item ", v10);
            }
        }
        return v2;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$de_attribute_value.invokeStatic(object2);
    }
}

