/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;

public final class common$compare_byte_arrays
extends AFunction
implements IFn.OOL {
    public static long invokeStatic(Object a, Object b) {
        long l;
        block4: {
            Object object = a;
            a = null;
            Object a2 = object;
            Object object2 = b;
            b = null;
            int len = ((byte[])a2).length;
            Object b2 = object2;
            long lencomp = (long)len - (long)((byte[])b2).length;
            if (lencomp == 0L) {
                long c;
                long pos = 0L;
                while (true) {
                    if (pos == (long)len) {
                        l = 0L;
                        break block4;
                    }
                    c = RT.uncheckedLongCast((Object)RT.aget((byte[])((byte[])a2), (int)((int)pos))) - RT.uncheckedLongCast((Object)RT.aget((byte[])((byte[])b2), (int)((int)pos)));
                    if (c != 0L) break;
                    ++pos;
                }
                l = c;
            } else {
                l = lencomp;
            }
        }
        return l;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(common$compare_byte_arrays.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$compare_byte_arrays.invokeStatic(object3, object4);
    }
}

