/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.iter.Iter;

public final class iter$filter$advance__11765
extends AFunction {
    Object p;

    public iter$filter$advance__11765(Object object) {
        this.p = object;
    }

    public Object invoke(Object iter2) {
        Object object;
        block2: {
            Object object2 = iter2;
            iter2 = null;
            Object iter3 = object2;
            while (true) {
                Object object3 = iter3;
                if (object3 == null || object3 == Boolean.FALSE) break;
                Object object4 = ((IFn)this.p).invoke(((Iter)iter3).get());
                if (object4 != null && object4 != Boolean.FALSE) {
                    object = iter3;
                    iter3 = null;
                    break block2;
                }
                Object object5 = iter3;
                iter3 = null;
                iter3 = ((Iter)object5).next();
            }
            object = null;
        }
        return object;
    }
}

