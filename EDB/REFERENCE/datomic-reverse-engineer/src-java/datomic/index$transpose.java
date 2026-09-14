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
import datomic.impl.db.IDatum;

public final class index$transpose
extends AFunction {
    public static final Var const__12 = RT.var((String)"datomic.index", (String)"transposed-data");

    public static Object invokeStatic(Object data2) {
        int n = RT.count((Object)data2);
        long[] es = Numbers.long_array((Object)n);
        int[] as = Numbers.int_array((Object)n);
        Object[] vs = RT.object_array((Object)n);
        long[] ts = Numbers.long_array((Object)n);
        boolean[] ops = Numbers.boolean_array((Object)n);
        long n__5742__auto__15248 = n;
        for (long i = 0L; i < n__5742__auto__15248; ++i) {
            Object d = RT.nth((Object)data2, (int)RT.uncheckedIntCast((long)i));
            RT.aset((long[])es, (int)((int)i), (long)((IDatum)d).getE());
            RT.aset((int[])as, (int)((int)i), (int)((IDatum)d).getA());
            RT.aset((Object[])vs, (int)((int)i), (Object)((IDatum)d).getV());
            RT.aset((long[])ts, (int)((int)i), (long)((IDatum)d).getT());
            Object object = d;
            d = null;
            RT.aset((boolean[])ops, (int)((int)i), (boolean)((IDatum)object).isAssertion());
        }
        long[] lArray = es;
        es = null;
        int[] nArray = as;
        as = null;
        Object[] objectArray = vs;
        vs = null;
        long[] lArray2 = ts;
        ts = null;
        boolean[] blArray = ops;
        ops = null;
        return ((IFn)const__12.getRawRoot()).invoke((Object)lArray, (Object)nArray, (Object)objectArray, (Object)lArray2, (Object)blArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$transpose.invokeStatic(object2);
    }
}

