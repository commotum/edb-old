/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class index$strdiff
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not=");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic(Object mins, Object maxs) {
        String string;
        block1: {
            void var1_1;
            for (long i = 0L; i < (long)((String)maxs).length(); ++i) {
                boolean or__5238__auto__15390 = Util.equiv((long)i, (long)((String)mins).length());
                Object object = or__5238__auto__15390 ? (or__5238__auto__15390 ? Boolean.TRUE : Boolean.FALSE) : ((IFn)const__3.getRawRoot()).invoke((Object)Character.valueOf(((String)mins).charAt(RT.uncheckedIntCast((long)i))), (Object)Character.valueOf(((String)maxs).charAt(RT.uncheckedIntCast((long)i))));
                if (object == null || object == Boolean.FALSE) continue;
                string = ((String)maxs).substring(RT.uncheckedIntCast((long)0L), RT.uncheckedIntCast((long)(i + 1L)));
                break block1;
            }
            string = var1_1;
        }
        return string;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$strdiff.invokeStatic(object3, object4);
    }
}

