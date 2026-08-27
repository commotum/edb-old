/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$valid_version
extends AFunction
implements IFn.OL {
    public static final Object const__1 = 1L;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{1L, 2L});
    public static final Var const__5 = RT.var((String)"datomic.error", (String)"state");
    public static final Keyword const__6 = RT.keyword((String)"db.error", (String)"index-version");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"version"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static long invokeStatic(Object root_map) {
        Object object;
        Object or__5238__auto__15286;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = root_map;
        root_map = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = or__5238__auto__15286 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__15286;
            or__5238__auto__15286 = null;
        } else {
            object = const__1;
        }
        Object version2 = object;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke((Object)const__4, version2);
        if (object5 != null && object5 != Boolean.FALSE) {
        } else {
            ((IFn)const__5.getRawRoot()).invoke((Object)const__6, ((IFn)const__7.getRawRoot()).invoke((Object)"This version of Datomic cannot read index version ", version2));
        }
        Object var1_1 = null;
        return ((Number)version2).longValue();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return new Long(index$valid_version.invokeStatic(object2));
    }

    public final long invokePrim(Object object) {
        Object object2 = object;
        object = null;
        return index$valid_version.invokeStatic(object2);
    }
}

