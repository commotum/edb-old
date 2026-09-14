/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.data$fn__9449;
import datomic.data$loading__6434__auto____9447;
import java.util.concurrent.Callable;

public class data__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__6;
    public static final AFn const__135;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new data$loading__6434__auto____9447()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new data$fn__9449())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__6);
        Var var2 = var;
        var.bindRoot((Object)const__135);
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.data");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.data", (String)"table");
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    }

    public static void __init1() {
        const__135 = (AFn)RT.vector((Object[])new Object[]{35L, 47L, 22L, 72L, 51L, 21L, 9L, 94L, 50L, 79L, 4L, 127L, 102L, 36L, 26L, 80L, 77L, 85L, 5L, 112L, 62L, 38L, 28L, 60L, 25L, 1L, 8L, 40L, 113L, 86L, 69L, 14L, 23L, 104L, 7L, 32L, 30L, 57L, 19L, 111L, 84L, 82L, 6L, 95L, 107L, 126L, 34L, 24L, 105L, 75L, 29L, 59L, 12L, 87L, 92L, 2L, 117L, 71L, 100L, 76L, 123L, 53L, 88L, 119L, 90L, 78L, 109L, 58L, 73L, 18L, 39L, 67L, 64L, 20L, 93L, 91L, 74L, 106L, 66L, 45L, 13L, 96L, 114L, 43L, 54L, 3L, 46L, 97L, 61L, 89L, 27L, 108L, 44L, 110L, 70L, 101L, 122L, 125L, 55L, 48L, 31L, 16L, 11L, 17L, 33L, 42L, 115L, 98L, 81L, 37L, 65L, 63L, 15L, 116L, 49L, 103L, 0L, 83L, 52L, 120L, 121L, 118L, 68L, 41L, 10L, 124L, 99L, 56L});
    }

    static {
        data__init.__init0();
        data__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.data__init").getClassLoader());
        try {
            data__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

