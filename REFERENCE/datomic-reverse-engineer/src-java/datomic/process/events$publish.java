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
package datomic.process;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class events$publish
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.process.events", (String)"subscribers-ref");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object e) {
        Object subscribers;
        Object n;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = e;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = n = object2;
        n = null;
        Object object4 = subscribers = RT.get((Object)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot()), (Object)object3);
        subscribers = null;
        Object seq_10842 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object4));
        Object chunk_10843 = null;
        long count_10844 = 0L;
        long i_10845 = 0L;
        while (true) {
            Object f;
            Object temp__5457__auto__10848;
            if (i_10845 < count_10844) {
                Object f2;
                Object object5 = f2 = ((Indexed)chunk_10843).nth(RT.intCast((long)i_10845));
                f2 = null;
                ((IFn)object5).invoke(e);
                Object object6 = seq_10842;
                seq_10842 = null;
                Object object7 = chunk_10843;
                chunk_10843 = null;
                ++i_10845;
                chunk_10843 = object7;
                seq_10842 = object6;
                continue;
            }
            Object object8 = seq_10842;
            seq_10842 = null;
            Object object9 = temp__5457__auto__10848 = ((IFn)const__4.getRawRoot()).invoke(object8);
            if (object9 == null || object9 == Boolean.FALSE) break;
            Object object10 = temp__5457__auto__10848;
            temp__5457__auto__10848 = null;
            Object seq_108422 = object10;
            Object object11 = ((IFn)const__9.getRawRoot()).invoke(seq_108422);
            if (object11 != null && object11 != Boolean.FALSE) {
                Object c__5719__auto__10847 = ((IFn)const__10.getRawRoot()).invoke(seq_108422);
                Object object12 = seq_108422;
                seq_108422 = null;
                Object object13 = c__5719__auto__10847;
                Object object14 = c__5719__auto__10847;
                c__5719__auto__10847 = null;
                i_10845 = RT.intCast((long)0L);
                count_10844 = RT.intCast((int)RT.count((Object)object14));
                chunk_10843 = object13;
                seq_10842 = ((IFn)const__11.getRawRoot()).invoke(object12);
                continue;
            }
            Object object15 = f = ((IFn)const__14.getRawRoot()).invoke(seq_108422);
            f = null;
            ((IFn)object15).invoke(e);
            Object object16 = seq_108422;
            seq_108422 = null;
            i_10845 = 0L;
            count_10844 = 0L;
            chunk_10843 = null;
            seq_10842 = ((IFn)const__15.getRawRoot()).invoke(object16);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return events$publish.invokeStatic(object2);
    }
}

