/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;

public final class datalog$fn__18233$hashy__18330
extends AFunction {
    Object bound;
    Object ident;

    public datalog$fn__18233$hashy__18330(Object object, Object object2) {
        this.bound = object;
        this.ident = object2;
    }

    public Object invoke(Object y) {
        long h = 0L;
        for (long i = 0L; i < (long)((Object[])this.bound).length; ++i) {
            Object b;
            Object object = b = RT.aget((Object[])((Object[])this.bound), (int)((int)i));
            b = null;
            h = object != null && object != Boolean.FALSE ? (long)Util.hashCombine((int)RT.uncheckedIntCast((long)h), (int)Util.hash((Object)((IFn)this.ident).invoke((Object)Numbers.num((long)i), RT.nth((Object)y, (int)RT.uncheckedIntCast((long)i))))) : h;
        }
        return Numbers.num((long)h);
    }
}

