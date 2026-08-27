/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.HashSet;

public final class datalog$project
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__13 = RT.var((String)"datomic.datalog", (String)"tuple");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object xs, Object xbinds, Object ybinds) {
        HashSet<Object> hashSet;
        if (Util.equiv((Object)xbinds, (Object)ybinds)) {
            hashSet = xs;
            xs = null;
        } else {
            Object object = xbinds;
            xbinds = null;
            Object xb = ((IFn)const__1.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke());
            Object yb = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(), ybinds);
            HashSet<Object> ret = new HashSet<Object>();
            HashSet<Object> hashSet2 = xs;
            xs = null;
            Object seq_18387 = ((IFn)const__3.getRawRoot()).invoke((Object)hashSet2);
            Object chunk_18388 = null;
            long count_18389 = 0L;
            long i_18390 = 0L;
            while (true) {
                Object temp__5457__auto__18395;
                if (i_18390 < count_18389) {
                    Object x = ((Indexed)chunk_18388).nth(RT.uncheckedIntCast((long)i_18390));
                    Object[] tos = RT.object_array((Object)RT.count((Object)ybinds));
                    long n__5742__auto__18392 = RT.count((Object)ybinds);
                    for (long i = 0L; i < n__5742__auto__18392; ++i) {
                        RT.aset((Object[])tos, (int)((int)i), (Object)RT.nth((Object)x, (int)RT.uncheckedIntCast((Object)((Number)((IFn)xb).invoke(((IFn)yb).invoke((Object)Numbers.num((long)i)))))));
                    }
                    Object[] objectArray = tos;
                    tos = null;
                    Boolean bl = ret.add(((IFn)const__13.getRawRoot()).invoke((Object)objectArray)) ? Boolean.TRUE : Boolean.FALSE;
                    Object object2 = seq_18387;
                    seq_18387 = null;
                    Object object3 = chunk_18388;
                    chunk_18388 = null;
                    ++i_18390;
                    chunk_18388 = object3;
                    seq_18387 = object2;
                    continue;
                }
                Object object4 = seq_18387;
                seq_18387 = null;
                Object object5 = temp__5457__auto__18395 = ((IFn)const__3.getRawRoot()).invoke(object4);
                if (object5 == null || object5 == Boolean.FALSE) break;
                Object object6 = temp__5457__auto__18395;
                temp__5457__auto__18395 = null;
                Object seq_183872 = object6;
                Object object7 = ((IFn)const__14.getRawRoot()).invoke(seq_183872);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object c__5719__auto__18393 = ((IFn)const__15.getRawRoot()).invoke(seq_183872);
                    Object object8 = seq_183872;
                    seq_183872 = null;
                    Object object9 = c__5719__auto__18393;
                    Object object10 = c__5719__auto__18393;
                    c__5719__auto__18393 = null;
                    i_18390 = (int)0L;
                    count_18389 = RT.count((Object)object10);
                    chunk_18388 = object9;
                    seq_18387 = ((IFn)const__16.getRawRoot()).invoke(object8);
                    continue;
                }
                Object x = ((IFn)const__17.getRawRoot()).invoke(seq_183872);
                Object[] tos = RT.object_array((Object)RT.count((Object)ybinds));
                long n__5742__auto__18394 = RT.count((Object)ybinds);
                for (long i = 0L; i < n__5742__auto__18394; ++i) {
                    RT.aset((Object[])tos, (int)((int)i), (Object)RT.nth((Object)x, (int)RT.uncheckedIntCast((Object)((Number)((IFn)xb).invoke(((IFn)yb).invoke((Object)Numbers.num((long)i)))))));
                }
                Object[] objectArray = tos;
                tos = null;
                Boolean bl = ret.add(((IFn)const__13.getRawRoot()).invoke((Object)objectArray)) ? Boolean.TRUE : Boolean.FALSE;
                Object object11 = seq_183872;
                seq_183872 = null;
                i_18390 = 0L;
                count_18389 = 0L;
                chunk_18388 = null;
                seq_18387 = ((IFn)const__18.getRawRoot()).invoke(object11);
            }
            hashSet = ret;
            ret = null;
        }
        return hashSet;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datalog$project.invokeStatic(object4, object5, object6);
    }
}

