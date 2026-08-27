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
import java.io.File;

public final class common$delete_file_recursively
extends RestFn {
    public static final Var const__2 = RT.var((String)"clojure.java.io", (String)"file");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"delete-file-recursively");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__14 = RT.var((String)"clojure.java.io", (String)"delete-file");

    public static Object invokeStatic(Object f, ISeq p__9175) {
        ISeq vec__9176;
        ISeq iSeq = p__9175;
        p__9175 = null;
        ISeq iSeq2 = vec__9176 = iSeq;
        vec__9176 = null;
        Object silently = RT.nth((Object)iSeq2, (int)RT.uncheckedIntCast((long)0L), null);
        Object object = f;
        f = null;
        Object f2 = ((IFn)const__2.getRawRoot()).invoke(object);
        if (((File)f2).isDirectory()) {
            Object seq_9179 = ((IFn)const__3.getRawRoot()).invoke((Object)((File)f2).listFiles());
            Object chunk_9180 = null;
            long count_9181 = 0L;
            long i_9182 = 0L;
            while (true) {
                Object child;
                Object temp__5457__auto__9185;
                if (i_9182 < count_9181) {
                    Object child2;
                    Object object2 = child2 = ((Indexed)chunk_9180).nth(RT.uncheckedIntCast((long)i_9182));
                    child2 = null;
                    ((IFn)const__5.getRawRoot()).invoke(object2, silently);
                    Object object3 = seq_9179;
                    seq_9179 = null;
                    Object object4 = chunk_9180;
                    chunk_9180 = null;
                    ++i_9182;
                    chunk_9180 = object4;
                    seq_9179 = object3;
                    continue;
                }
                Object object5 = seq_9179;
                seq_9179 = null;
                Object object6 = temp__5457__auto__9185 = ((IFn)const__3.getRawRoot()).invoke(object5);
                if (object6 == null || object6 == Boolean.FALSE) break;
                Object object7 = temp__5457__auto__9185;
                temp__5457__auto__9185 = null;
                Object seq_91792 = object7;
                Object object8 = ((IFn)const__7.getRawRoot()).invoke(seq_91792);
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object c__5719__auto__9184 = ((IFn)const__8.getRawRoot()).invoke(seq_91792);
                    Object object9 = seq_91792;
                    seq_91792 = null;
                    Object object10 = c__5719__auto__9184;
                    Object object11 = c__5719__auto__9184;
                    c__5719__auto__9184 = null;
                    i_9182 = (int)0L;
                    count_9181 = RT.count((Object)object11);
                    chunk_9180 = object10;
                    seq_9179 = ((IFn)const__9.getRawRoot()).invoke(object9);
                    continue;
                }
                Object object12 = child = ((IFn)const__12.getRawRoot()).invoke(seq_91792);
                child = null;
                ((IFn)const__5.getRawRoot()).invoke(object12, silently);
                Object object13 = seq_91792;
                seq_91792 = null;
                i_9182 = 0L;
                count_9181 = 0L;
                chunk_9180 = null;
                seq_9179 = ((IFn)const__13.getRawRoot()).invoke(object13);
            }
        }
        Object object14 = f2;
        f2 = null;
        Object object15 = silently;
        silently = null;
        return ((IFn)const__14.getRawRoot()).invoke(object14, object15);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return common$delete_file_recursively.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

