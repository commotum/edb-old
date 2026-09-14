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

public final class fulltext$add_data_to_writer
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"datomic.lucene", (String)"add-document");
    public static final Var const__4 = RT.var((String)"datomic.fulltext-index", (String)"datum->doc");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object writer2, Object data2) {
        Object object = data2;
        data2 = null;
        Object seq_14553 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object chunk_14554 = null;
        long count_14555 = 0L;
        long i_14556 = 0L;
        while (true) {
            Object datum2;
            Object temp__5457__auto__14559;
            if (i_14556 < count_14555) {
                Object datum3;
                Object object2 = datum3 = ((Indexed)chunk_14554).nth(RT.intCast((long)i_14556));
                datum3 = null;
                ((IFn)const__3.getRawRoot()).invoke(writer2, ((IFn)const__4.getRawRoot()).invoke(object2));
                Object object3 = seq_14553;
                seq_14553 = null;
                Object object4 = chunk_14554;
                chunk_14554 = null;
                ++i_14556;
                chunk_14554 = object4;
                seq_14553 = object3;
                continue;
            }
            Object object5 = seq_14553;
            seq_14553 = null;
            Object object6 = temp__5457__auto__14559 = ((IFn)const__0.getRawRoot()).invoke(object5);
            if (object6 == null || object6 == Boolean.FALSE) break;
            Object object7 = temp__5457__auto__14559;
            temp__5457__auto__14559 = null;
            Object seq_145532 = object7;
            Object object8 = ((IFn)const__6.getRawRoot()).invoke(seq_145532);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object c__5719__auto__14558 = ((IFn)const__7.getRawRoot()).invoke(seq_145532);
                Object object9 = seq_145532;
                seq_145532 = null;
                Object object10 = c__5719__auto__14558;
                Object object11 = c__5719__auto__14558;
                c__5719__auto__14558 = null;
                i_14556 = RT.intCast((long)0L);
                count_14555 = RT.intCast((int)RT.count((Object)object11));
                chunk_14554 = object10;
                seq_14553 = ((IFn)const__8.getRawRoot()).invoke(object9);
                continue;
            }
            Object object12 = datum2 = ((IFn)const__11.getRawRoot()).invoke(seq_145532);
            datum2 = null;
            ((IFn)const__3.getRawRoot()).invoke(writer2, ((IFn)const__4.getRawRoot()).invoke(object12));
            Object object13 = seq_145532;
            seq_145532 = null;
            i_14556 = 0L;
            count_14555 = 0L;
            chunk_14554 = null;
            seq_14553 = ((IFn)const__12.getRawRoot()).invoke(object13);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$add_data_to_writer.invokeStatic(object3, object4);
    }
}

