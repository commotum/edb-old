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

public final class index$dir_partition_size
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"DIR_PARTITION_SIZE");

    public static Object invokeStatic(Object idx) {
        Object scale;
        Object object = scale = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.indexDirScale");
        scale = null;
        Object object2 = idx;
        idx = null;
        return Numbers.unchecked_multiply((Object)object, (Object)((IFn)object2).invoke(const__2.getRawRoot()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$dir_partition_size.invokeStatic(object2);
    }
}

