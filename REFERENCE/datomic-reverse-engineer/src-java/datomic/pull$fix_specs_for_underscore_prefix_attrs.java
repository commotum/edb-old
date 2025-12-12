/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class pull$fix_specs_for_underscore_prefix_attrs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"forward");
    public static final Keyword const__4 = RT.keyword(null, (String)"reverse");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"dissoc");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"_keys"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object p__19003, Object db2) {
        Object object;
        Object temp__5455__auto__19006;
        Object map__19004;
        Object object2;
        Object object3 = p__19003;
        p__19003 = null;
        Object map__190042 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__190042);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__190042;
            map__190042 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__190042;
            map__190042 = null;
        }
        Object m = map__19004 = object2;
        Object forward = RT.get((Object)map__19004, (Object)const__3);
        Object object6 = map__19004;
        map__19004 = null;
        Object reverse = RT.get((Object)object6, (Object)const__4);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = db2;
        db2 = null;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object object9 = temp__5455__auto__19006 = object8;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = temp__5455__auto__19006;
            temp__5455__auto__19006 = null;
            Object ks = object10;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__3;
            Object object11 = forward;
            forward = null;
            objectArray[1] = ((IFn)const__6.getRawRoot()).invoke(object11, ((IFn)const__7.getRawRoot()).invoke(reverse, ks));
            objectArray[2] = const__4;
            Object object12 = reverse;
            reverse = null;
            Object object13 = ks;
            ks = null;
            objectArray[3] = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), object12, object13);
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = m;
            m = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$fix_specs_for_underscore_prefix_attrs.invokeStatic(object3, object4);
    }
}

