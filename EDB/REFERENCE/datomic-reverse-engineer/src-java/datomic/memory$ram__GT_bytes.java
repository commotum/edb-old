/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class memory$ram__GT_bytes
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"trim");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"last");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"drop-last");
    public static final AFn const__13 = (AFn)RT.map((Object[])new Object[]{Character.valueOf('B'), 1L, Character.valueOf('K'), 1024L, Character.valueOf('M'), 0x100000L, Character.valueOf('G'), 0x40000000L});

    public static Object invokeStatic(Object ram) {
        AFn conversion;
        Object object = ram;
        ram = null;
        Object ram2 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object units = ((IFn)const__1.getRawRoot()).invoke((Object)((String)ram2).toUpperCase());
        Object object2 = ram2;
        ram2 = null;
        int value = Integer.parseInt((String)((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(object2)));
        AFn aFn = conversion = const__13;
        conversion = null;
        Object object3 = units;
        units = null;
        return Numbers.multiply((long)value, (Object)RT.get((Object)aFn, (Object)object3));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory$ram__GT_bytes.invokeStatic(object2);
    }
}

