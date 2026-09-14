/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.cloudwatch.model.DimensionFilter
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
import com.amazonaws.services.cloudwatch.model.DimensionFilter;

public final class cloudwatch$fn__22926
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"name");
    public static final Keyword const__3 = RT.keyword(null, (String)"value");
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"name"), RT.keyword(null, (String)"value")});
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__9 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"name"), RT.keyword(null, (String)"value")});
    public static final Keyword const__11 = RT.keyword(null, (String)"keys");
    public static final Keyword const__12 = RT.keyword(null, (String)"constructor");
    public static final Object const__13 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DimensionFilter");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__15 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__17 = RT.classForName((String)"java.lang.String");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"value"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__22928;
        Object object = temp__5457__auto__22928 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__22928;
            temp__5457__auto__22928 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__9;
            objectArray[1] = const__10;
            objectArray[2] = const__11;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__12;
            objectArray[5] = const__13;
            throw (Throwable)((IFn)const__6.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        DimensionFilter o = new DimensionFilter();
        Object object5 = ((IFn)const__14.getRawRoot()).invoke(m, (Object)const__2);
        if (object5 != null && object5 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = m;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            Object object8 = v = object7;
            v = null;
            Object object9 = k = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)o), (Object)const__2, object8, const__17);
            k = null;
            o.setName((String)object9);
        }
        Object object10 = ((IFn)const__14.getRawRoot()).invoke(m, (Object)const__3);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object11 = m;
            m = null;
            Object object12 = iLookupThunk.get(object11);
            if (iLookupThunk == object12) {
                __thunk__1__ = __site__1__.fault(object11);
                object12 = __thunk__1__.get(object11);
            }
            Object object13 = v = object12;
            v = null;
            Object object14 = k = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)o), (Object)const__3, object13, const__17);
            k = null;
            o.setValue((String)object14);
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cloudwatch$fn__22926.invokeStatic(object3, object4);
    }
}

