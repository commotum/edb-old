/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$L
 *  clojure.lang.IFn$OOL
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
import java.util.Random;

public final class math$uniform
extends AFunction
implements IFn.L,
IFn.OOL {
    public static final Var const__0 = RT.var((String)"datomic.math", (String)"*rnd*");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__4 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<"), Symbol.intern(null, (String)"lo"), Symbol.intern(null, (String)"hi")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 25}));

    public static long invokeStatic(Object lo, Object hi) {
        if (!Numbers.lt((Object)lo, (Object)hi)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__2.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__3.getRawRoot()).invoke(const__4))));
        }
        Object object = lo;
        Object object2 = hi;
        hi = null;
        Object object3 = lo;
        lo = null;
        return RT.longCast((double)Math.floor(Numbers.add((Object)object, (double)Numbers.multiply((double)((Random)const__0.get()).nextDouble(), (Object)Numbers.minus((Object)object2, (Object)object3)))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(math$uniform.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return math$uniform.invokeStatic(object3, object4);
    }

    public static long invokeStatic() {
        return ((Random)const__0.get()).nextLong();
    }

    public Object invoke() {
        return new Long(math$uniform.invokeStatic());
    }

    public final long invokePrim() {
        return math$uniform.invokeStatic();
    }
}

