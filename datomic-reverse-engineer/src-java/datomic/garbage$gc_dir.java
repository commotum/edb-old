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
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.garbage$gc_dir$fn__19852;
import datomic.garbage$gc_dir$fn__19856;

public final class garbage$gc_dir
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.garbage", (String)"gc-get-node");
    public static final Keyword const__1 = RT.keyword(null, (String)"count");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__3 = 0L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Keyword const__6 = RT.keyword(null, (String)"complete");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"last");
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"count"), 0L, RT.keyword(null, (String)"complete"), Boolean.TRUE});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"end"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object cluster2, Object uuid, Object tstamp) {
        AFn aFn;
        Object temp__5455__auto__19859;
        Object object = uuid;
        uuid = null;
        Object object2 = temp__5455__auto__19859 = ((IFn)const__0.getRawRoot()).invoke(cluster2, object);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__19859;
            temp__5455__auto__19859 = null;
            Object dir = object3;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            IFn iFn = (IFn)const__2.getRawRoot();
            Object object4 = cluster2;
            cluster2 = null;
            garbage$gc_dir$fn__19852 garbage$gc_dir$fn__19852 = new garbage$gc_dir$fn__19852(object4, tstamp);
            IFn iFn2 = (IFn)const__4.getRawRoot();
            garbage$gc_dir$fn__19856 garbage$gc_dir$fn__19856 = new garbage$gc_dir$fn__19856(tstamp);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = dir;
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            objectArray[1] = iFn.invoke((Object)garbage$gc_dir$fn__19852, const__3, iFn2.invoke((Object)garbage$gc_dir$fn__19856, object6));
            objectArray[2] = const__6;
            ILookupThunk iLookupThunk2 = __thunk__2__;
            IFn iFn3 = (IFn)const__10.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__1__;
            Object object7 = dir;
            dir = null;
            Object object8 = iLookupThunk3.get(object7);
            if (iLookupThunk3 == object8) {
                __thunk__1__ = __site__1__.fault(object7);
                object8 = __thunk__1__.get(object7);
            }
            Object object9 = iFn3.invoke(object8);
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__2__ = __site__2__.fault(object9);
                object10 = __thunk__2__.get(object9);
            }
            Object object11 = tstamp;
            tstamp = null;
            objectArray[3] = Numbers.isNeg((long)Util.compare((Object)object10, (Object)object11)) ? Boolean.TRUE : Boolean.FALSE;
            aFn = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            aFn = const__11;
        }
        return aFn;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return garbage$gc_dir.invokeStatic(object4, object5, object6);
    }
}

