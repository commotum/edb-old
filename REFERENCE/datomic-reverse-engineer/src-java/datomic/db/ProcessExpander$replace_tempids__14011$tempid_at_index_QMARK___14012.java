/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ProcessExpander$replace_tempids__14011$tempid_at_index_QMARK___14012
extends AFunction {
    Object attr;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"tempid?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleRefOffsets"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public ProcessExpander$replace_tempids__14011$tempid_at_index_QMARK___14012(Object object) {
        this.attr = object;
    }

    public Object invoke(Object idx, Object elem) {
        Object object;
        Object and__5236__auto__14015;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = this.attr;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = idx;
        idx = null;
        Object object5 = and__5236__auto__14015 = iFn.invoke(object3, object4);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object and__5236__auto__14014;
            Object object6 = and__5236__auto__14014 = elem;
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = elem;
                elem = null;
                object = ((IFn.LO)const__2.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object7)));
            } else {
                object = and__5236__auto__14014;
                and__5236__auto__14014 = null;
            }
        } else {
            object = and__5236__auto__14015;
            Object var3_3 = null;
        }
        return object;
    }
}

