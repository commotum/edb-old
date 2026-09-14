/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class crypto$xor_arrays
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"b1")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 14})), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"b2")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 25}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"aset-byte");

    public static Object invokeStatic(Object b1, Object b2) {
        if ((long)RT.count((Object)b1) != (long)RT.count((Object)b2)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        }
        byte[] result2 = Numbers.byte_array((Object)RT.count((Object)b1));
        long n = 0L;
        while (n < (long)RT.count((Object)b1)) {
            ((IFn)const__8.getRawRoot()).invoke((Object)result2, (Object)Numbers.num((long)n), (Object)Numbers.num((long)Numbers.xor((long)RT.longCast((Object)RT.aget((byte[])((byte[])b1), (int)RT.intCast((long)n))), (long)RT.longCast((Object)RT.aget((byte[])((byte[])b2), (int)RT.intCast((long)n))))));
            n = Numbers.inc((long)n);
        }
        Object var2_2 = null;
        return result2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$xor_arrays.invokeStatic(object3, object4);
    }
}

