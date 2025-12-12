/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class logged$create_STAR_$fn__20184
extends AFunction {
    Object watches_ref;
    Object logged_atom;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"anom"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public logged$create_STAR_$fn__20184(Object object, Object object2) {
        this.watches_ref = object;
        this.logged_atom = object2;
    }

    public Object invoke(Object _, Object _2, Object old, Object object) {
        Object v3;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = object;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            v3 = null;
        } else {
            Object seq_20185 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.watches_ref));
            Object chunk_20186 = null;
            long count_20187 = 0L;
            long i_20188 = 0L;
            while (true) {
                Object v;
                Object temp__5804__auto__20197;
                if (i_20188 < count_20187) {
                    Object v2;
                    Object vec__20189 = ((Indexed)chunk_20186).nth(RT.intCast((long)i_20188));
                    Object k = RT.nth((Object)vec__20189, (int)RT.intCast((long)0L), null);
                    Object object4 = vec__20189;
                    vec__20189 = null;
                    Object object5 = v2 = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
                    v2 = null;
                    IFn iFn = (IFn)object5;
                    Object object6 = k;
                    k = null;
                    ILookupThunk iLookupThunk2 = __thunk__1__;
                    Object object7 = old;
                    Object object8 = iLookupThunk2.get(object7);
                    if (iLookupThunk2 == object8) {
                        __thunk__1__ = __site__1__.fault(object7);
                        object8 = __thunk__1__.get(object7);
                    }
                    ILookupThunk iLookupThunk3 = __thunk__2__;
                    Object object9 = object;
                    Object object10 = iLookupThunk3.get(object9);
                    if (iLookupThunk3 == object10) {
                        __thunk__2__ = __site__2__.fault(object9);
                        object10 = __thunk__2__.get(object9);
                    }
                    iFn.invoke(this.logged_atom, object6, object8, object10);
                    Object object11 = seq_20185;
                    seq_20185 = null;
                    Object object12 = chunk_20186;
                    chunk_20186 = null;
                    ++i_20188;
                    chunk_20186 = object12;
                    seq_20185 = object11;
                    continue;
                }
                Object object13 = seq_20185;
                seq_20185 = null;
                Object object14 = temp__5804__auto__20197 = ((IFn)const__1.getRawRoot()).invoke(object13);
                if (object14 == null || object14 == Boolean.FALSE) break;
                Object object15 = temp__5804__auto__20197;
                temp__5804__auto__20197 = null;
                Object seq_201852 = object15;
                Object object16 = ((IFn)const__9.getRawRoot()).invoke(seq_201852);
                if (object16 != null && object16 != Boolean.FALSE) {
                    Object c__6065__auto__20196 = ((IFn)const__10.getRawRoot()).invoke(seq_201852);
                    Object object17 = seq_201852;
                    seq_201852 = null;
                    Object object18 = c__6065__auto__20196;
                    Object object19 = c__6065__auto__20196;
                    c__6065__auto__20196 = null;
                    i_20188 = RT.intCast((long)0L);
                    count_20187 = RT.intCast((int)RT.count((Object)object19));
                    chunk_20186 = object18;
                    seq_20185 = ((IFn)const__11.getRawRoot()).invoke(object17);
                    continue;
                }
                Object vec__20192 = ((IFn)const__14.getRawRoot()).invoke(seq_201852);
                Object k = RT.nth((Object)vec__20192, (int)RT.intCast((long)0L), null);
                Object object20 = vec__20192;
                vec__20192 = null;
                Object object21 = v = RT.nth((Object)object20, (int)RT.intCast((long)1L), null);
                v = null;
                IFn iFn = (IFn)object21;
                Object object22 = k;
                k = null;
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object23 = old;
                Object object24 = iLookupThunk4.get(object23);
                if (iLookupThunk4 == object24) {
                    __thunk__3__ = __site__3__.fault(object23);
                    object24 = __thunk__3__.get(object23);
                }
                ILookupThunk iLookupThunk5 = __thunk__4__;
                Object object25 = object;
                Object object26 = iLookupThunk5.get(object25);
                if (iLookupThunk5 == object26) {
                    __thunk__4__ = __site__4__.fault(object25);
                    object26 = __thunk__4__.get(object25);
                }
                iFn.invoke(this.logged_atom, object22, object24, object26);
                Object object27 = seq_201852;
                seq_201852 = null;
                i_20188 = 0L;
                count_20187 = 0L;
                chunk_20186 = null;
                seq_20185 = ((IFn)const__15.getRawRoot()).invoke(object27);
            }
            v3 = null;
        }
        return v3;
    }
}

