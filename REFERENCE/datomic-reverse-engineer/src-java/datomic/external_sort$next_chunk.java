/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import datomic.iter.Iter;
import java.util.ArrayList;

public final class external_sort$next_chunk
extends AFunction {
    public static Object invokeStatic(Object max_size, Object sizer, Object iter2) {
        IPersistentVector iPersistentVector;
        block3: {
            block2: {
                Object iter3;
                ArrayList<Object> a;
                block1: {
                    Object object = iter2;
                    if (object == null || object == Boolean.FALSE) break block2;
                    a = new ArrayList<Object>();
                    long size = 0L;
                    Object object2 = iter2;
                    iter2 = null;
                    iter3 = object2;
                    while (true) {
                        Object inext2;
                        Object temp__5455__auto__14402;
                        Object item = ((Iter)iter3).get();
                        long size2 = RT.longCast((Object)Numbers.add((long)size, (Object)((IFn)sizer).invoke(item)));
                        Object object3 = item;
                        item = null;
                        Boolean bl = a.add(object3) ? Boolean.TRUE : Boolean.FALSE;
                        if (!Numbers.lt((long)size2, (Object)max_size)) break block1;
                        Object object4 = iter3;
                        iter3 = null;
                        Object object5 = temp__5455__auto__14402 = ((Iter)object4).next();
                        if (object5 == null || object5 == Boolean.FALSE) break;
                        Object object6 = temp__5455__auto__14402;
                        temp__5455__auto__14402 = null;
                        Object object7 = inext2 = object6;
                        inext2 = null;
                        iter3 = object7;
                        size = size2;
                    }
                    iPersistentVector = Tuple.create(a, null);
                    break block3;
                }
                Object object = iter3;
                iter3 = null;
                iPersistentVector = Tuple.create(a, (Object)((Iter)object).next());
                break block3;
            }
            iPersistentVector = null;
        }
        return iPersistentVector;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return external_sort$next_chunk.invokeStatic(object4, object5, object6);
    }
}

