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

public final class datalog$fn__18233$match_QMARK___18326
extends AFunction {
    Object bindings;
    Object ident;

    public datalog$fn__18233$match_QMARK___18326(Object object, Object object2) {
        this.bindings = object;
        this.ident = object2;
    }

    public Object invoke(Object x, Object y) {
        Boolean bl;
        block3: {
            long i = 0L;
            while (i < (long)((Object[])this.bindings).length) {
                Object b;
                Object object = b = RT.aget((Object[])((Object[])this.bindings), (int)((int)i));
                if (object != null && object != Boolean.FALSE) {
                    Object object2 = b;
                    b = null;
                    if (Util.equiv((Object)((IFn)this.ident).invoke((Object)Numbers.num((long)i), RT.nth((Object)x, (int)RT.uncheckedIntCast((long)i))), (Object)((IFn)this.ident).invoke((Object)Numbers.num((long)i), RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)object2)))))) {
                        ++i;
                        continue;
                    }
                    bl = Boolean.FALSE;
                    break block3;
                }
                ++i;
            }
            bl = Boolean.TRUE;
        }
        return bl;
    }
}

