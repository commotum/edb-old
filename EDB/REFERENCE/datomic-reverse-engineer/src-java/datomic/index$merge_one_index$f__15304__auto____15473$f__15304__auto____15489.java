/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489
extends AFunction {
    Object d;
    Object lt;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"fnext");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489(Object object, Object object2) {
        this.d = object;
        this.lt = object2;
    }

    public Object invoke(Object es, Object des) {
        while (true) {
            Object object;
            Object and__5236__auto__15491;
            Object object2 = and__5236__auto__15491 = ((IFn)const__0.getRawRoot()).invoke(des);
            if (object2 != null && object2 != Boolean.FALSE) {
                IFn iFn = (IFn)this.lt;
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object3 = ((IFn)const__2.getRawRoot()).invoke(des);
                Object object4 = iLookupThunk.get(object3);
                if (iLookupThunk == object4) {
                    __thunk__0__ = __site__0__.fault(object3);
                    object4 = __thunk__0__.get(object3);
                }
                object = iFn.invoke(object4, this.d);
            } else {
                object = and__5236__auto__15491;
                Object var3_3 = null;
            }
            if (object == null || object == Boolean.FALSE) break;
            Object object5 = es;
            es = null;
            Object object6 = ((IFn)const__3.getRawRoot()).invoke(object5, ((IFn)const__4.getRawRoot()).invoke(des));
            Object object7 = des;
            des = null;
            des = ((IFn)const__0.getRawRoot()).invoke(object7);
            es = object6;
        }
        Object object = es;
        es = null;
        Object object8 = des;
        des = null;
        return Tuple.create((Object)object, (Object)object8);
    }
}

