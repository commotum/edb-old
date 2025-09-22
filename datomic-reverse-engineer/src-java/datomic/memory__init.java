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
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.memory$aws_transactor_settings;
import datomic.memory$fn__20758;
import datomic.memory$loading__6434__auto____20756;
import datomic.memory$object_cache_max;
import datomic.memory$ram__GT_bytes;
import datomic.memory$segment_cache_max;
import datomic.memory$transactor_cache_bytes;
import datomic.memory$transactor_settings;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class memory__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__7;
    public static final Var const__11;
    public static final AFn const__12;
    public static final Var const__15;
    public static final AFn const__16;
    public static final Object const__17;
    public static final Var const__18;
    public static final AFn const__19;
    public static final Object const__20;
    public static final Var const__21;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__24;
    public static final Object const__25;
    public static final Var const__26;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__31;
    public static final Var const__71;
    public static final AFn const__73;
    public static final Var const__74;
    public static final AFn const__76;
    public static final Var const__77;
    public static final AFn const__79;
    public static final Var const__80;
    public static final AFn const__82;
    public static final Var const__83;
    public static final AFn const__84;
    public static final Var const__85;
    public static final AFn const__86;
    public static final Var const__87;
    public static final AFn const__89;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new memory$loading__6434__auto____20756()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new memory$fn__20758())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)96L, (long)0x100000L)));
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)64L, (long)1024L)));
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot(const__17);
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot(const__20);
        Var var9 = const__21;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)8000L, (long)60L)));
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot(const__25);
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__29);
        Var var14 = var13;
        var13.bindRoot((Object)new memory$ram__GT_bytes());
        Var var15 = const__30;
        var15.setMeta((IPersistentMap)const__31);
        Var var16 = var15;
        var15.bindRoot((Object)RT.mapUniqueKeys((Object[])new Object[]{"m1.large", Numbers.num((long)Numbers.multiply((long)7500L, (long)0x100000L)), "c1.medium", Numbers.num((long)Numbers.multiply((long)1700L, (long)0x100000L)), "r4.2xlarge", Numbers.num((long)Numbers.multiply((long)61L, (long)0x40000000L)), "r3.8xlarge", Numbers.num((long)Numbers.multiply((long)244L, (long)0x40000000L)), "m3.2xlarge", Numbers.num((long)Numbers.multiply((long)30L, (long)0x40000000L)), "i2.8xlarge", Numbers.num((long)Numbers.multiply((long)244L, (long)0x40000000L)), "c3.8xlarge", Numbers.num((long)Numbers.multiply((long)60L, (long)0x40000000L)), "t2.large", Numbers.num((long)Numbers.multiply((long)8L, (long)0x40000000L)), "m1.xlarge", Numbers.num((long)Numbers.multiply((long)15L, (long)0x40000000L)), "m4.4xlarge", Numbers.num((long)Numbers.multiply((long)64L, (long)0x40000000L)), "t2.2xlarge", Numbers.num((long)Numbers.multiply((long)32L, (long)0x40000000L)), "r4.16xlarge", Numbers.num((long)Numbers.multiply((long)488L, (long)0x40000000L)), "cr1.8xlarge", Numbers.num((long)Numbers.multiply((long)244L, (long)0x40000000L)), "t1.micro", Numbers.num((long)Numbers.multiply((long)613L, (long)0x100000L)), "c3.2xlarge", Numbers.num((long)Numbers.multiply((long)15L, (long)0x40000000L)), "c4.8xlarge", Numbers.num((long)Numbers.multiply((long)60L, (long)0x40000000L)), "m2.xlarge", Numbers.num((long)Numbers.multiply((long)17L, (long)0x40000000L)), "m1.small", Numbers.num((long)Numbers.multiply((long)1700L, (long)0x100000L)), "i2.4xlarge", Numbers.num((long)Numbers.multiply((long)122L, (long)0x40000000L)), "c3.4xlarge", Numbers.num((long)Numbers.multiply((long)30L, (long)0x40000000L)), "i3.16xlarge", Numbers.num((long)Numbers.multiply((long)488L, (long)0x40000000L)), "m4.2xlarge", Numbers.num((long)Numbers.multiply((long)32L, (long)0x40000000L)), "i3.4xlarge", Numbers.num((long)Numbers.multiply((long)122L, (long)0x40000000L)), "m4.10xlarge", Numbers.num((long)Numbers.multiply((long)160L, (long)0x40000000L)), "m1.medium", Numbers.num((long)Numbers.multiply((long)3750L, (long)0x100000L)), "c4.4xlarge", Numbers.num((long)Numbers.multiply((long)30L, (long)0x40000000L)), "hi1.4xlarge", Numbers.num((long)Numbers.multiply((long)60L, (long)0x40000000L)), "r3.xlarge", Numbers.num((long)Numbers.multiply((long)30L, (long)0x40000000L)), "c4.2xlarge", Numbers.num((long)Numbers.multiply((long)15L, (long)0x40000000L)), "i3.8xlarge", Numbers.num((long)Numbers.multiply((long)244L, (long)0x40000000L)), "t2.small", Numbers.num((long)Numbers.multiply((long)2L, (long)0x40000000L)), "r4.xlarge", Numbers.num((long)Numbers.multiply((long)30500L, (long)0x100000L)), "m3.xlarge", Numbers.num((long)Numbers.multiply((long)15L, (long)0x40000000L)), "r3.2xlarge", Numbers.num((long)Numbers.multiply((long)61L, (long)0x40000000L)), "r3.4xlarge", Numbers.num((long)Numbers.multiply((long)122L, (long)0x40000000L)), "t2.xlarge", Numbers.num((long)Numbers.multiply((long)16L, (long)0x40000000L)), "c4.xlarge", Numbers.num((long)Numbers.multiply((long)7500L, (long)0x100000L)), "m2.2xlarge", Numbers.num((long)Numbers.multiply((long)34L, (long)0x40000000L)), "m4.large", Numbers.num((long)Numbers.multiply((long)8L, (long)0x40000000L)), "m4.16xlarge", Numbers.num((long)Numbers.multiply((long)256L, (long)0x40000000L)), "r3.large", Numbers.num((long)Numbers.multiply((long)15L, (long)0x40000000L)), "i3.2xlarge", Numbers.num((long)Numbers.multiply((long)61L, (long)0x40000000L)), "c4.large", Numbers.num((long)Numbers.multiply((long)3750L, (long)0x100000L)), "m4.xlarge", Numbers.num((long)Numbers.multiply((long)16L, (long)0x40000000L)), "i2.2xlarge", Numbers.num((long)Numbers.multiply((long)61L, (long)0x40000000L)), "i3.xlarge", Numbers.num((long)Numbers.multiply((long)30500L, (long)0x100000L)), "m2.4xlarge", Numbers.num((long)Numbers.multiply((long)68L, (long)0x40000000L)), "r4.large", Numbers.num((long)Numbers.multiply((long)15250L, (long)0x100000L)), "c3.xlarge", Numbers.num((long)Numbers.multiply((long)7500L, (long)0x100000L)), "i3.large", Numbers.num((long)Numbers.multiply((long)15250L, (long)0x100000L)), "r4.8xlarge", Numbers.num((long)Numbers.multiply((long)244L, (long)0x40000000L)), "t2.medium", Numbers.num((long)Numbers.multiply((long)4L, (long)0x40000000L)), "c3.large", Numbers.num((long)Numbers.multiply((long)3750L, (long)0x100000L)), "m3.medium", Numbers.num((long)Numbers.multiply((long)3750L, (long)0x100000L)), "i2.xlarge", Numbers.num((long)Numbers.multiply((long)30L, (long)0x40000000L)), "cc2.8xlarge", Numbers.num((long)Numbers.multiply((long)60L, (long)0x40000000L)), "hs1.8xlarge", Numbers.num((long)Numbers.multiply((long)117L, (long)0x40000000L)), "m3.large", Numbers.num((long)Numbers.multiply((long)7500L, (long)0x100000L)), "c1.xlarge", Numbers.num((long)Numbers.multiply((long)7L, (long)0x40000000L)), "r4.4xlarge", Numbers.num((long)Numbers.multiply((long)122L, (long)0x40000000L))}));
        Var var17 = const__71;
        var17.setMeta((IPersistentMap)const__73);
        Var var18 = var17;
        var17.bindRoot((Object)new memory$segment_cache_max());
        Var var19 = const__74;
        var19.setMeta((IPersistentMap)const__76);
        Var var20 = var19;
        var19.bindRoot((Object)new memory$object_cache_max());
        Var var21 = const__77;
        var21.setMeta((IPersistentMap)const__79);
        Var var22 = var21;
        var21.bindRoot((Object)new memory$transactor_settings());
        Var var23 = const__80;
        var23.setMeta((IPersistentMap)const__82);
        Var var24 = var23;
        var23.bindRoot((Object)new memory$aws_transactor_settings());
        Var var25 = const__83;
        var25.setMeta((IPersistentMap)const__84);
        Var var26 = var25;
        var25.bindRoot(const__77.getRawRoot());
        Var var27 = const__85;
        var27.setMeta((IPersistentMap)const__86);
        Var var28 = var27;
        var27.bindRoot(const__80.getRawRoot());
        Var var29 = const__87;
        var29.setMeta((IPersistentMap)const__89);
        Var var30 = var29;
        var29.bindRoot((Object)new memory$transactor_cache_bytes());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.memory");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.memory", (String)"MINIMUM_VM_SIZE");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.memory", (String)"SEGMENT_SIZE");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.memory", (String)"DATOMS_PER_SEGMENT");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__17 = 8000L;
        const__18 = RT.var((String)"datomic.memory", (String)"DATOM_OBJECT_SIZE");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__20 = 60L;
        const__21 = RT.var((String)"datomic.memory", (String)"SEGMENT_OBJECT_SIZE");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.memory", (String)"TRANSACTOR_SPARE_MEM");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__25 = 0L;
        const__26 = RT.var((String)"datomic.memory", (String)"ram->bytes");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ram")))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.memory", (String)"aws-instance-mem");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__71 = RT.var((String)"datomic.memory", (String)"segment-cache-max");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"segment-memory-size")))), RT.keyword(null, (String)"column"), 1});
        const__74 = RT.var((String)"datomic.memory", (String)"object-cache-max");
        const__76 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"virtual-memory-size")))), RT.keyword(null, (String)"column"), 1});
        const__77 = RT.var((String)"datomic.memory", (String)"transactor-settings");
        const__79 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ram-bytes")), Tuple.create((Object)Symbol.intern(null, (String)"ram-bytes"), (Object)Symbol.intern(null, (String)"memidx-bytes")))), RT.keyword(null, (String)"column"), 1});
        const__80 = RT.var((String)"datomic.memory", (String)"aws-transactor-settings");
        const__82 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"instance-type")))), RT.keyword(null, (String)"column"), 1});
        const__83 = RT.var((String)"datomic.memory", (String)"peer-settings");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.memory", (String)"aws-peer-settings");
        const__86 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__87 = RT.var((String)"datomic.memory", (String)"transactor-cache-bytes");
        const__89 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"memidx-max")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        memory__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.memory__init").getClassLoader());
        try {
            memory__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

