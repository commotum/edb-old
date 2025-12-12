/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$avet_sort_and_process_datoms$fn__15538;
import java.io.File;

public final class index$avet_sort_and_process_datoms
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"datomic.external-sort-datoms", (String)"consume-sorted-datoms");
    public static final Keyword const__2 = RT.keyword(null, (String)"cmp");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"avet-cmp");
    public static final Keyword const__4 = RT.keyword(null, (String)"dir");
    public static final Keyword const__5 = RT.keyword(null, (String)"prog-fn");
    public static final Keyword const__6 = RT.keyword(null, (String)"max-chunk-size");

    public static Object invokeStatic(Object datoms2, Object f) {
        Object dir = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.indexWorkDir");
        Boolean bl = ((File)dir).mkdirs() ? Boolean.TRUE : Boolean.FALSE;
        Object object = datoms2;
        datoms2 = null;
        Object[] objectArray = new Object[8];
        objectArray[0] = const__2;
        objectArray[1] = const__3.getRawRoot();
        objectArray[2] = const__4;
        Object object2 = dir;
        dir = null;
        objectArray[3] = object2;
        objectArray[4] = const__5;
        objectArray[5] = new index$avet_sort_and_process_datoms$fn__15538();
        objectArray[6] = const__6;
        objectArray[7] = Numbers.num((long)Numbers.unchecked_multiply((long)(10L * 1000L), (long)1000L));
        Object object3 = f;
        f = null;
        return ((IFn)const__1.getRawRoot()).invoke(object, (Object)RT.mapUniqueKeys((Object[])objectArray), object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$avet_sort_and_process_datoms.invokeStatic(object3, object4);
    }
}

