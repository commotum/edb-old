/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Indexed
 *  clojure.lang.LazilyPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Indexed;
import clojure.lang.LazilyPersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.HashSet;

public final class extensions$project
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object xs, Object binds) {
        HashSet<IPersistentVector> hashSet;
        if (Util.equiv((Object)binds, (Object)((IFn)const__1.getRawRoot()).invoke((Object)RT.count((Object)binds)))) {
            hashSet = xs;
            xs = null;
        } else {
            HashSet<IPersistentVector> ret = new HashSet<IPersistentVector>();
            HashSet<IPersistentVector> hashSet2 = xs;
            xs = null;
            Object seq_17995 = ((IFn)const__3.getRawRoot()).invoke((Object)hashSet2);
            Object chunk_17996 = null;
            long count_17997 = 0L;
            long i_17998 = 0L;
            while (true) {
                Object temp__5457__auto__18003;
                if (i_17998 < count_17997) {
                    Object x = ((Indexed)chunk_17996).nth(RT.intCast((long)i_17998));
                    Object[] tos = RT.object_array((Object)RT.count((Object)binds));
                    long n__5742__auto__18000 = RT.count((Object)binds);
                    for (long i = 0L; i < n__5742__auto__18000; ++i) {
                        RT.aset((Object[])tos, (int)RT.intCast((long)i), (Object)RT.nth((Object)x, (int)RT.intCast((Object)((Number)RT.nth((Object)binds, (int)RT.intCast((long)i))))));
                    }
                    Object[] objectArray = tos;
                    tos = null;
                    Boolean bl = ret.add(LazilyPersistentVector.createOwning((Object[])objectArray)) ? Boolean.TRUE : Boolean.FALSE;
                    Object object = seq_17995;
                    seq_17995 = null;
                    Object object2 = chunk_17996;
                    chunk_17996 = null;
                    ++i_17998;
                    chunk_17996 = object2;
                    seq_17995 = object;
                    continue;
                }
                Object object = seq_17995;
                seq_17995 = null;
                Object object3 = temp__5457__auto__18003 = ((IFn)const__3.getRawRoot()).invoke(object);
                if (object3 == null || object3 == Boolean.FALSE) break;
                Object object4 = temp__5457__auto__18003;
                temp__5457__auto__18003 = null;
                Object seq_179952 = object4;
                Object object5 = ((IFn)const__12.getRawRoot()).invoke(seq_179952);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object c__5719__auto__18001 = ((IFn)const__13.getRawRoot()).invoke(seq_179952);
                    Object object6 = seq_179952;
                    seq_179952 = null;
                    Object object7 = c__5719__auto__18001;
                    Object object8 = c__5719__auto__18001;
                    c__5719__auto__18001 = null;
                    i_17998 = RT.intCast((long)0L);
                    count_17997 = RT.intCast((int)RT.count((Object)object8));
                    chunk_17996 = object7;
                    seq_17995 = ((IFn)const__14.getRawRoot()).invoke(object6);
                    continue;
                }
                Object x = ((IFn)const__15.getRawRoot()).invoke(seq_179952);
                Object[] tos = RT.object_array((Object)RT.count((Object)binds));
                long n__5742__auto__18002 = RT.count((Object)binds);
                for (long i = 0L; i < n__5742__auto__18002; ++i) {
                    RT.aset((Object[])tos, (int)RT.intCast((long)i), (Object)RT.nth((Object)x, (int)RT.intCast((Object)((Number)RT.nth((Object)binds, (int)RT.intCast((long)i))))));
                }
                Object[] objectArray = tos;
                tos = null;
                Boolean bl = ret.add(LazilyPersistentVector.createOwning((Object[])objectArray)) ? Boolean.TRUE : Boolean.FALSE;
                Object object9 = seq_179952;
                seq_179952 = null;
                i_17998 = 0L;
                count_17997 = 0L;
                chunk_17996 = null;
                seq_17995 = ((IFn)const__16.getRawRoot()).invoke(object9);
            }
            hashSet = ret;
            Object var2_2 = null;
        }
        return hashSet;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extensions$project.invokeStatic(object3, object4);
    }
}

