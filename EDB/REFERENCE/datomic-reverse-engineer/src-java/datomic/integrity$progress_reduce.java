/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$progress_reduce$pf__21975;

public final class integrity$progress_reduce
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"progress");
    public static final Keyword const__4 = RT.keyword(null, (String)"n");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__7 = 0L;

    public static Object invokeStatic(Object f, Object val, Object p__21972, Object coll) {
        Object object;
        Object object2;
        Object object3 = p__21972;
        p__21972 = null;
        Object map__21973 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21973);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__21973;
            map__21973 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__21973;
            map__21973 = null;
        }
        Object map__219732 = object2;
        Object progress = RT.get((Object)map__219732, (Object)const__3);
        Object object6 = map__219732;
        map__219732 = null;
        Object n = RT.get((Object)object6, (Object)const__4);
        Object object7 = progress;
        if (object7 != null && object7 != Boolean.FALSE) {
            integrity$progress_reduce$pf__21975 pf;
            Object object8 = f;
            f = null;
            Object object9 = progress;
            progress = null;
            Object object10 = n;
            n = null;
            integrity$progress_reduce$pf__21975 integrity$progress_reduce$pf__21975 = pf = new integrity$progress_reduce$pf__21975(object8, object9, object10);
            pf = null;
            Object object11 = val;
            val = null;
            Object object12 = coll;
            coll = null;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)integrity$progress_reduce$pf__21975, (Object)Tuple.create((Object)const__7, (Object)object11), object12));
        } else {
            Object object13 = f;
            f = null;
            Object object14 = val;
            val = null;
            Object object15 = coll;
            coll = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object13, object14, object15);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return integrity$progress_reduce.invokeStatic(object5, object6, object7, object8);
    }
}

