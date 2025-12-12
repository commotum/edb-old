/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.require$maybe_require$fn__636;
import datomic.require$maybe_require$fn__638;

public final class require$maybe_require
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(ISeq syms) {
        Object v1;
        String string = System.getProperty("datomic.disableAllExtensions");
        if (string != null && string != Boolean.FALSE) {
            v1 = null;
        } else {
            ISeq iSeq = syms;
            syms = null;
            Object seq_632 = ((IFn)const__0.getRawRoot()).invoke((Object)iSeq);
            Object chunk_633 = null;
            long count_634 = 0L;
            long i_635 = 0L;
            while (true) {
                Object s;
                Object temp__5457__auto__642;
                if (i_635 < count_634) {
                    Object s2;
                    Object object = s2 = ((Indexed)chunk_633).nth(RT.intCast((long)i_635));
                    s2 = null;
                    ((IFn)new require$maybe_require$fn__636(object)).invoke();
                    Object object2 = seq_632;
                    seq_632 = null;
                    Object object3 = chunk_633;
                    chunk_633 = null;
                    ++i_635;
                    chunk_633 = object3;
                    seq_632 = object2;
                    continue;
                }
                Object object = seq_632;
                seq_632 = null;
                Object object4 = temp__5457__auto__642 = ((IFn)const__0.getRawRoot()).invoke(object);
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = temp__5457__auto__642;
                temp__5457__auto__642 = null;
                Object seq_6322 = object5;
                Object object6 = ((IFn)const__4.getRawRoot()).invoke(seq_6322);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object c__5719__auto__641 = ((IFn)const__5.getRawRoot()).invoke(seq_6322);
                    Object object7 = seq_6322;
                    seq_6322 = null;
                    Object object8 = c__5719__auto__641;
                    Object object9 = c__5719__auto__641;
                    c__5719__auto__641 = null;
                    i_635 = RT.intCast((long)0L);
                    count_634 = RT.intCast((int)RT.count((Object)object9));
                    chunk_633 = object8;
                    seq_632 = ((IFn)const__6.getRawRoot()).invoke(object7);
                    continue;
                }
                Object object10 = s = ((IFn)const__9.getRawRoot()).invoke(seq_6322);
                s = null;
                ((IFn)new require$maybe_require$fn__638(object10)).invoke();
                Object object11 = seq_6322;
                seq_6322 = null;
                i_635 = 0L;
                count_634 = 0L;
                chunk_633 = null;
                seq_632 = ((IFn)const__10.getRawRoot()).invoke(object11);
            }
            v1 = null;
        }
        return v1;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return require$maybe_require.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

