/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.config_ext$fn__9445;
import datomic.config_ext$loading__6434__auto____9443;
import java.util.concurrent.Callable;

public class config_ext__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__4;
    public static final AFn const__5;
    public static final AFn const__6;
    public static final Var const__7;
    public static final AFn const__10;
    public static final AFn const__124;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new config_ext$loading__6434__auto____9443()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new config_ext$fn__9445())));
            v2 = null;
        }
        Object object3 = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, (Object)const__5, (Object)const__6);
        Var var = const__7;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)Numbers.int_array((int)RT.intCast((long)256L), (Object)const__124));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.config-ext");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.require", (String)"maybe-require");
        const__4 = (AFn)Symbol.intern(null, (String)"datomic.data");
        const__5 = (AFn)Symbol.intern(null, (String)"datomic.obscure");
        const__6 = (AFn)Symbol.intern(null, (String)"datomic.license-inline");
        const__7 = RT.var((String)"datomic.config-ext", (String)"flags");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    }

    public static void __init1() {
        const__124 = (AFn)RT.vector((Object[])new Object[]{208L, 88L, 27L, 222L, 15L, 80L, 222L, 157L, 41L, 210L, 76L, 79L, 86L, 81L, 117L, 214L, 232L, 178L, 117L, 252L, 227L, 33L, 65L, 29L, 129L, 154L, 214L, 185L, 49L, 106L, 31L, 175L, 217L, 18L, 50L, 239L, 64L, 210L, 190L, 162L, 45L, 81L, 59L, 145L, 244L, 48L, 197L, 89L, 153L, 103L, 218L, 214L, 243L, 8L, 178L, 233L, 160L, 13L, 106L, 141L, 8L, 130L, 128L, 48L, 187L, 123L, 226L, 100L, 13L, 3L, 236L, 227L, 128L, 92L, 49L, 126L, 197L, 8L, 156L, 127L, 22L, 92L, 29L, 13L, 74L, 15L, 3L, 142L, 233L, 225L, 236L, 243L, 198L, 39L, 48L, 187L, 80L, 85L, 174L, 74L, 26L, 76L, 13L, 198L, 210L, 60L, 218L, 244L, 45L, 84L, 88L, 201L, 37L, 110L, 100L, 26L, 93L, 252L, 85L, 128L, 162L, 71L, 3L, 232L, 15L, 3L, 236L, 111L, 17L, 49L, 202L, 95L, 147L, 160L, 64L, 80L, 18L, 59L, 13L, 31L, 145L, 142L, 84L, 100L, 79L, 186L, 99L, 145L, 243L, 187L, 226L, 157L, 214L, 27L, 26L, 62L, 129L, 182L, 156L, 100L, 39L, 239L, 86L, 29L, 118L, 89L, 234L, 2L, 242L, 236L, 89L, 222L, 89L, 242L, 62L, 13L, 48L, 231L, 93L, 225L, 154L, 22L, 111L, 79L, 49L, 126L, 214L, 130L, 74L, 145L, 41L, 95L, 229L, 175L, 13L, 174L, 201L, 234L, 3L, 156L, 229L, 141L, 107L, 98L, 15L, 162L, 216L, 218L, 74L, 30L, 123L, 1L, 142L, 65L, 80L, 60L, 160L, 236L, 64L, 126L, 188L, 2L, 38L, 78L, 93L, 160L, 22L, 131L, 89L, 162L, 142L, 149L, 126L, 201L, 49L, 131L, 234L, 142L, 141L, 138L, 88L, 145L, 59L, 33L, 139L, 136L, 210L, 216L, 8L, 216L, 172L, 49L, 202L, 211L, 69L, 85L});
    }

    static {
        config_ext__init.__init0();
        config_ext__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.config_ext__init").getClassLoader());
        try {
            config_ext__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

