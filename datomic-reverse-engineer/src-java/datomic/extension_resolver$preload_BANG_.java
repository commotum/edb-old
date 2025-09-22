/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;

public final class extension_resolver$preload_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"user-namespaces");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object path2) {
        Object object = path2;
        path2 = null;
        Object seq_14327 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
        Object chunk_14328 = null;
        long count_14329 = 0L;
        long i_14330 = 0L;
        while (true) {
            Object ns;
            Object temp__5457__auto__14333;
            if (i_14330 < count_14329) {
                Object ns2;
                Object object2 = ns2 = ((Indexed)chunk_14328).nth(RT.intCast((long)i_14330));
                ns2 = null;
                ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object2));
                Object object3 = seq_14327;
                seq_14327 = null;
                Object object4 = chunk_14328;
                chunk_14328 = null;
                ++i_14330;
                chunk_14328 = object4;
                seq_14327 = object3;
                continue;
            }
            Object object5 = seq_14327;
            seq_14327 = null;
            Object object6 = temp__5457__auto__14333 = ((IFn)const__0.getRawRoot()).invoke(object5);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__14333;
            temp__5457__auto__14333 = null;
            Object seq_143272 = object7;
            Object object8 = ((IFn)const__7.getRawRoot()).invoke(seq_143272);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__14332 = ((IFn)const__8.getRawRoot()).invoke(seq_143272);
                Object object9 = seq_143272;
                seq_143272 = null;
                Object object10 = c__5719__auto__14332;
                Object object11 = c__5719__auto__14332;
                c__5719__auto__14332 = null;
                i_14330 = RT.intCast((long)0L);
                count_14329 = RT.intCast((int)RT.count((Object)object11));
                chunk_14328 = object10;
                seq_14327 = ((IFn)const__9.getRawRoot()).invoke(object9);
                continue;
            }
            Object object12 = ns = ((IFn)const__12.getRawRoot()).invoke(seq_143272);
            ns = null;
            ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object12));
            Object object13 = seq_143272;
            seq_143272 = null;
            i_14330 = 0L;
            count_14329 = 0L;
            chunk_14328 = null;
            seq_14327 = ((IFn)const__13.getRawRoot()).invoke(object13);
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$preload_BANG_.invokeStatic(object2);
    }
}

