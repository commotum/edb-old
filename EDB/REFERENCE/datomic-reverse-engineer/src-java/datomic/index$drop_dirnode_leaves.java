/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$drop_dirnode_leaves$fn__15594;
import datomic.index.DirNode;

public final class index$drop_dirnode_leaves
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"keydata"), (Object)RT.keyword(null, (String)"segids"), (Object)RT.keyword(null, (String)"offsets"), (Object)RT.keyword(null, (String)"counts"), (Object)RT.keyword(null, (String)"garbage"));
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"repeat");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__12 = RT.var((String)"datomic.index", (String)"dir-node");
    public static final Var const__13 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"into-array");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"garbage"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"keydata"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"keydata"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"segids"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"offsets"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"counts"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"garbage"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"garbage"));
    static ILookupThunk __thunk__7__ = __site__7__;

    public static Object invokeStatic(Object dirnode, Object drop_QMARK_) {
        IPersistentVector iPersistentVector;
        Object object = drop_QMARK_;
        drop_QMARK_ = null;
        Object result2 = ((IFn)const__0.getRawRoot()).invoke((Object)new index$drop_dirnode_leaves$fn__15594(dirnode, object), ((IFn)const__1.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)PersistentVector.EMPTY)), ((IFn)const__9.getRawRoot()).invoke((Object)RT.count((Object)((DirNode)dirnode).keydata)));
        IFn iFn = (IFn)const__11.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = result2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = iFn.invoke(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            IFn iFn2 = (IFn)const__11.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = result2;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            Object object7 = iFn2.invoke(object6);
            if (object7 != null && object7 != Boolean.FALSE) {
                IFn iFn3 = (IFn)const__12.getRawRoot();
                IFn iFn4 = (IFn)const__13.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object8 = result2;
                Object object9 = iLookupThunk3.get(object8);
                if (iLookupThunk3 == object9) {
                    __thunk__2__ = __site__2__.fault(object8);
                    object9 = __thunk__2__.get(object8);
                }
                Object object10 = iFn4.invoke(object9);
                IFn iFn5 = (IFn)const__14.getRawRoot();
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object11 = result2;
                Object object12 = iLookupThunk4.get(object11);
                if (iLookupThunk4 == object12) {
                    __thunk__3__ = __site__3__.fault(object11);
                    object12 = __thunk__3__.get(object11);
                }
                Object object13 = iFn5.invoke(object12);
                IFn iFn6 = (IFn)const__15.getRawRoot();
                ILookupThunk iLookupThunk5 = __thunk__4__;
                Object object14 = result2;
                Object object15 = iLookupThunk5.get(object14);
                if (iLookupThunk5 == object15) {
                    __thunk__4__ = __site__4__.fault(object14);
                    object15 = __thunk__4__.get(object14);
                }
                Object object16 = iFn6.invoke(Integer.TYPE, object15);
                IFn iFn7 = (IFn)const__15.getRawRoot();
                ILookupThunk iLookupThunk6 = __thunk__5__;
                Object object17 = result2;
                Object object18 = iLookupThunk6.get(object17);
                if (iLookupThunk6 == object18) {
                    __thunk__5__ = __site__5__.fault(object17);
                    object18 = __thunk__5__.get(object17);
                }
                Object object19 = iFn3.invoke(object10, object13, object16, iFn7.invoke(Integer.TYPE, object18));
                ILookupThunk iLookupThunk7 = __thunk__6__;
                Object object20 = result2;
                result2 = null;
                Object object21 = iLookupThunk7.get(object20);
                if (iLookupThunk7 == object21) {
                    __thunk__6__ = __site__6__.fault(object20);
                    object21 = __thunk__6__.get(object20);
                }
                iPersistentVector = Tuple.create((Object)object19, (Object)object21);
            } else {
                ILookupThunk iLookupThunk8 = __thunk__7__;
                Object object22 = result2;
                result2 = null;
                Object object23 = iLookupThunk8.get(object22);
                if (iLookupThunk8 == object23) {
                    __thunk__7__ = __site__7__.fault(object22);
                    object23 = __thunk__7__.get(object22);
                }
                iPersistentVector = Tuple.create(null, (Object)object23);
            }
        } else {
            Object object24 = dirnode;
            dirnode = null;
            iPersistentVector = Tuple.create((Object)object24, null);
        }
        return iPersistentVector;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$drop_dirnode_leaves.invokeStatic(object3, object4);
    }
}

