/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Connection;
import java.util.List;

public final class peer$transact_all
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"next");
    public static final Keyword const__12 = RT.keyword(null, (String)"ok");

    public static Object invokeStatic(Object conn, Object txdata) {
        Object object = txdata;
        txdata = null;
        Object seq_21663 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object chunk_21664 = null;
        long count_21665 = 0L;
        long i_21666 = 0L;
        while (true) {
            Object tx;
            Object temp__5457__auto__21669;
            if (i_21666 < count_21665) {
                Object tx2;
                Object object2 = tx2 = ((Indexed)chunk_21664).nth(RT.intCast((long)i_21666));
                tx2 = null;
                ((IFn)const__3.getRawRoot()).invoke(((Connection)conn).transact((List)object2));
                Object object3 = seq_21663;
                seq_21663 = null;
                Object object4 = chunk_21664;
                chunk_21664 = null;
                ++i_21666;
                chunk_21664 = object4;
                seq_21663 = object3;
                continue;
            }
            Object object5 = seq_21663;
            seq_21663 = null;
            Object object6 = temp__5457__auto__21669 = ((IFn)const__0.getRawRoot()).invoke(object5);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__21669;
            temp__5457__auto__21669 = null;
            Object seq_216632 = object7;
            Object object8 = ((IFn)const__5.getRawRoot()).invoke(seq_216632);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__21668 = ((IFn)const__6.getRawRoot()).invoke(seq_216632);
                Object object9 = seq_216632;
                seq_216632 = null;
                Object object10 = c__5719__auto__21668;
                Object object11 = c__5719__auto__21668;
                c__5719__auto__21668 = null;
                i_21666 = RT.intCast((long)0L);
                count_21665 = RT.intCast((int)RT.count((Object)object11));
                chunk_21664 = object10;
                seq_21663 = ((IFn)const__7.getRawRoot()).invoke(object9);
                continue;
            }
            Object object12 = tx = ((IFn)const__10.getRawRoot()).invoke(seq_216632);
            tx = null;
            ((IFn)const__3.getRawRoot()).invoke(((Connection)conn).transact((List)object12));
            Object object13 = seq_216632;
            seq_216632 = null;
            i_21666 = 0L;
            count_21665 = 0L;
            chunk_21664 = null;
            seq_21663 = ((IFn)const__11.getRawRoot()).invoke(object13);
        }
        return const__12;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$transact_all.invokeStatic(object3, object4);
    }
}

