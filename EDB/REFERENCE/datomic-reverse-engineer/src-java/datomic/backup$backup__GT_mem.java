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
import clojure.lang.Var;

public final class backup$backup__GT_mem
extends AFunction {
    public static final Keyword const__0 = RT.keyword((String)"backup", (String)"version");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Object const__2 = 1L;
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"require-keys");
    public static final Keyword const__6 = RT.keyword((String)"log", (String)"version");
    public static final Keyword const__7 = RT.keyword(null, (String)"unknown");
    public static final Keyword const__8 = RT.keyword((String)"index", (String)"version");
    public static final Var const__10 = RT.var((String)"clojure.set", (String)"rename-keys");
    public static final Var const__11 = RT.var((String)"clojure.set", (String)"map-invert");
    public static final Var const__12 = RT.var((String)"datomic.backup", (String)"v1->v2-keymap");
    public static final Var const__14 = RT.var((String)"datomic.error", (String)"state");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"backup-version");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__17 = RT.var((String)"datomic.backup", (String)"mem-keys");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"backup", (String)"version"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"backup", (String)"version"));
    static ILookupThunk __thunk__1__ = __site__1__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object roots) {
        Object object;
        Object object2;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = roots;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (object4 != null && object4 != Boolean.FALSE) {
            object2 = roots;
            roots = null;
        } else {
            Object object5 = roots;
            roots = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke(object5, (Object)const__0, const__2);
        }
        Object roots2 = object2;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object6 = roots2;
        Object object7 = iLookupThunk2.get(object6);
        if (iLookupThunk2 == object7) {
            __thunk__1__ = __site__1__.fault(object6);
            object7 = __thunk__1__.get(object6);
        }
        long version2 = RT.longCast((Object)object7);
        IFn iFn = (IFn)const__4.getRawRoot();
        long G__20215 = version2;
        switch ((int)G__20215) {
            case 1: {
                if (1L != G__20215) break;
                Object object8 = roots2;
                roots2 = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object8, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__7);
                return iFn.invoke(object, const__17.getRawRoot());
            }
            case 2: {
                if (2L != G__20215) break;
                Object object9 = roots2;
                roots2 = null;
                object = ((IFn)const__10.getRawRoot()).invoke(object9, ((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot()));
                return iFn.invoke(object, const__17.getRawRoot());
            }
            case 3: {
                if (3L != G__20215) break;
                Object object10 = roots2;
                roots2 = null;
                object = ((IFn)const__10.getRawRoot()).invoke(object10, ((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot()));
                return iFn.invoke(object, const__17.getRawRoot());
            }
        }
        object = ((IFn)const__14.getRawRoot()).invoke((Object)const__15, ((IFn)const__16.getRawRoot()).invoke((Object)"This version of Datomic cannot read backup version ", (Object)Numbers.num((long)version2)));
        return iFn.invoke(object, const__17.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$backup__GT_mem.invokeStatic(object2);
    }
}

