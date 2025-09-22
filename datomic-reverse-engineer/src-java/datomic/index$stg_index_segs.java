/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;
import datomic.index.Index;
import datomic.index.RootNode;

public final class index$stg_index_segs
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Object const__6 = 0L;

    public static Object invokeStatic(Object olookup, Object index2, Object part_size) {
        Object object;
        Number or__5238__auto__15641;
        Number number;
        Object object2 = index2;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = index2;
            index2 = null;
            Object root = ((Index)object3).root;
            int n_dirs = ((Object[])((RootNode)root).dirids).length;
            if ((long)n_dirs == 0L) {
                number = null;
            } else {
                Object last_dir;
                long last_idx = (long)n_dirs - 1L;
                Object object4 = root;
                root = null;
                Object object5 = olookup;
                olookup = null;
                Object object6 = last_dir = ((IFn)const__3.getRawRoot()).invoke(object4, (Object)Numbers.num((long)last_idx), object5, (Object)Boolean.TRUE);
                last_dir = null;
                int last_dir_n_segs = ((Object[])((DirNode)object6).segids).length;
                Object object7 = part_size;
                part_size = null;
                number = Numbers.unchecked_add((Object)Numbers.unchecked_multiply((Object)object7, (long)last_idx), (long)last_dir_n_segs);
            }
        } else {
            number = null;
        }
        Number number2 = or__5238__auto__15641 = number;
        if (number2 != null && number2 != Boolean.FALSE) {
            object = or__5238__auto__15641;
            or__5238__auto__15641 = null;
        } else {
            object = const__6;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$stg_index_segs.invokeStatic(object4, object5, object6);
    }
}

