/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
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
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class memory$transactor_settings
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.memory", (String)"transactor-settings");
    public static final Object const__1 = 0x4000000L;
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__14 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"*"), 256L, Symbol.intern((String)"math", (String)"M")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 19})), Symbol.intern(null, (String)"ram-bytes")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 16}));
    public static final Keyword const__15 = RT.keyword(null, (String)"memidx-max");
    public static final Keyword const__17 = RT.keyword(null, (String)"xmx");

    public static Object invokeStatic(Object ram_bytes, Object memidx_bytes) {
        Number available_bytes = Numbers.minus((Object)Numbers.minus((Object)Numbers.minus((Object)ram_bytes, (long)0x6000000L), (long)0L), (Object)memidx_bytes);
        long transactor_segment = RT.longCast((double)Numbers.multiply((Object)available_bytes, (double)0.5));
        Number number = available_bytes;
        available_bytes = null;
        long transactor_object = RT.longCast((double)Numbers.multiply((Object)number, (double)0.5));
        if (!Numbers.lt((long)Numbers.multiply((long)256L, (long)0x100000L), (Object)ram_bytes)) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__12.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"not enough memory to run a transactor", (Object)"\n", ((IFn)const__13.getRawRoot()).invoke(const__14))));
        }
        Object[] objectArray = new Object[4];
        objectArray[0] = const__15;
        Object object = memidx_bytes;
        memidx_bytes = null;
        objectArray[1] = ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.quotient((Object)object, (long)0x100000L), (Object)"m");
        objectArray[2] = const__17;
        Object object2 = ram_bytes;
        ram_bytes = null;
        objectArray[3] = ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.quotient((Object)object2, (long)0x100000L), (Object)"m");
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return memory$transactor_settings.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object ram_bytes) {
        Object object = ram_bytes;
        ram_bytes = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory$transactor_settings.invokeStatic(object2);
    }
}

