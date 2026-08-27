/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
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
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.memory_size$array_type;
import datomic.memory_size$fn__380;
import datomic.memory_size$fn__384;
import datomic.memory_size$fn__387;
import datomic.memory_size$fn__406;
import datomic.memory_size$fn__408;
import datomic.memory_size$fn__410;
import datomic.memory_size$fn__412;
import datomic.memory_size$fn__414;
import datomic.memory_size$fn__416;
import datomic.memory_size$fn__418;
import datomic.memory_size$fn__420;
import datomic.memory_size$fn__422;
import datomic.memory_size$fn__424;
import datomic.memory_size$fn__428;
import datomic.memory_size$fn__430;
import datomic.memory_size$fn__432;
import datomic.memory_size$fn__434;
import datomic.memory_size$fn__436;
import datomic.memory_size$fn__444;
import datomic.memory_size$fn__446;
import datomic.memory_size$fn__450;
import datomic.memory_size$fn__452;
import datomic.memory_size$fn__454;
import datomic.memory_size$fn__456;
import datomic.memory_size$fn__458;
import datomic.memory_size$fn__460;
import datomic.memory_size$fn__462;
import datomic.memory_size$fn__464;
import datomic.memory_size$fn__466;
import datomic.memory_size$fn__468;
import datomic.memory_size$fn__470;
import datomic.memory_size$fn__472;
import datomic.memory_size$handle_primitive_arrays;
import datomic.memory_size$loading__6434__auto____378;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class memory_size__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final ISeq const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final AFn const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final AFn const__27;
    public static final AFn const__28;
    public static final Keyword const__29;
    public static final Var const__30;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__36;
    public static final Object const__37;
    public static final Var const__38;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__41;
    public static final Object const__42;
    public static final Var const__43;
    public static final AFn const__44;
    public static final Object const__45;
    public static final Var const__46;
    public static final AFn const__47;
    public static final Object const__48;
    public static final Var const__49;
    public static final AFn const__50;
    public static final Object const__51;
    public static final Var const__52;
    public static final AFn const__55;
    public static final Var const__56;
    public static final AFn const__58;
    public static final Var const__59;
    public static final Object const__60;
    public static final Object const__61;
    public static final Object const__62;
    public static final Object const__63;
    public static final Object const__64;
    public static final Object const__65;
    public static final Object const__66;
    public static final Object const__67;
    public static final Object const__68;
    public static final Object const__69;
    public static final Object const__70;
    public static final Object const__71;
    public static final Object const__72;
    public static final Object const__73;
    public static final Object const__74;
    public static final Object const__75;
    public static final Object const__76;
    public static final Object const__77;
    public static final Object const__78;
    public static final Object const__79;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new memory_size$loading__6434__auto____378()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new memory_size$fn__380())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new memory_size$fn__384()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__22, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__12.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__6})))), new memory_size$fn__387()})));
        Object object9 = ((IFn)const__30.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__31;
        Var var = const__32;
        var.setMeta((IPersistentMap)const__36);
        Var var2 = var;
        var.bindRoot(const__37);
        Var var3 = const__38;
        var3.setMeta((IPersistentMap)const__39);
        Var var4 = var3;
        var3.bindRoot(const__37);
        Var var5 = const__40;
        var5.setMeta((IPersistentMap)const__41);
        Var var6 = var5;
        var5.bindRoot(const__42);
        Var var7 = const__43;
        var7.setMeta((IPersistentMap)const__44);
        Var var8 = var7;
        var7.bindRoot(const__45);
        Var var9 = const__46;
        var9.setMeta((IPersistentMap)const__47);
        Var var10 = var9;
        var9.bindRoot(const__48);
        Var var11 = const__49;
        var11.setMeta((IPersistentMap)const__50);
        Var var12 = var11;
        var11.bindRoot(const__51);
        Var var13 = const__52;
        var13.setMeta((IPersistentMap)const__55);
        Var var14 = var13;
        var13.bindRoot((Object)new memory_size$array_type());
        Var var15 = const__56;
        var15.setMeta((IPersistentMap)const__58);
        Var var16 = var15;
        var15.bindRoot((Object)new memory_size$handle_primitive_arrays());
        const__56.setMacro();
        Object v27 = null;
        Var var17 = const__56;
        Object object10 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Long.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__406()}));
        Object object11 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Integer.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__408()}));
        Object object12 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Boolean.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__410()}));
        Object object13 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Double.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__412()}));
        Object object14 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Float.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__414()}));
        Object object15 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Short.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__416()}));
        Object object16 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Character.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__418()}));
        Object object17 = ((IFn)const__59.getRawRoot()).invoke(((IFn)const__52.getRawRoot()).invoke(Byte.TYPE), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__420()}));
        Object object18 = ((IFn)const__59.getRawRoot()).invoke(null, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__422()}));
        Object object19 = ((IFn)const__59.getRawRoot()).invoke(const__60, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__424()}));
        Object object20 = ((IFn)const__59.getRawRoot()).invoke(const__61, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__428()}));
        Object object21 = ((IFn)const__59.getRawRoot()).invoke(const__62, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__430()}));
        Object object22 = ((IFn)const__59.getRawRoot()).invoke(const__63, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__432()}));
        Object object23 = ((IFn)const__59.getRawRoot()).invoke(const__64, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__434()}));
        Object object24 = ((IFn)const__59.getRawRoot()).invoke(const__65, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__436()}));
        Object object25 = ((IFn)const__59.getRawRoot()).invoke(const__66, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__444()}));
        Object object26 = ((IFn)const__59.getRawRoot()).invoke(const__67, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__446()}));
        Object object27 = ((IFn)const__59.getRawRoot()).invoke(const__68, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__450()}));
        Object object28 = ((IFn)const__59.getRawRoot()).invoke(const__69, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__452()}));
        Object object29 = ((IFn)const__59.getRawRoot()).invoke(const__70, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__454()}));
        Object object30 = ((IFn)const__59.getRawRoot()).invoke(const__71, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__456()}));
        Object object31 = ((IFn)const__59.getRawRoot()).invoke(const__72, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__458()}));
        Object object32 = ((IFn)const__59.getRawRoot()).invoke(const__73, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__460()}));
        Object object33 = ((IFn)const__59.getRawRoot()).invoke(const__74, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__462()}));
        Object object34 = ((IFn)const__59.getRawRoot()).invoke(const__75, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__464()}));
        Object object35 = ((IFn)const__59.getRawRoot()).invoke(const__76, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__466()}));
        Object object36 = ((IFn)const__59.getRawRoot()).invoke(const__77, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__468()}));
        Object object37 = ((IFn)const__59.getRawRoot()).invoke(const__78, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__470()}));
        Object object38 = ((IFn)const__59.getRawRoot()).invoke(const__79, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new memory_size$fn__472()}));
        Object v58 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.memory-size");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.memory_size.MemorySize");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.memory-size", (String)"MemorySize");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"memory-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.memory_size.MemorySize"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.memory_size.MemorySize")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"memory-size"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"memory-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return best guess of the memory size of an object."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__21 = RT.keyword(null, (String)"memory-size");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"memory-size"), RT.keyword(null, (String)"memory-size")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"memory-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"memory-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return best guess of the memory size of an object."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__31 = (AFn)Symbol.intern(null, (String)"MemorySize");
        const__32 = RT.var((String)"datomic.memory-size", (String)"REFERENCE");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__37 = 8L;
        const__38 = RT.var((String)"datomic.memory-size", (String)"LONG");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.memory-size", (String)"INT");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__42 = 4L;
        const__43 = RT.var((String)"datomic.memory-size", (String)"CHAR");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__45 = 2L;
        const__46 = RT.var((String)"datomic.memory-size", (String)"ARRAY");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__48 = 16L;
        const__49 = RT.var((String)"datomic.memory-size", (String)"STRING");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__51 = 56L;
        const__52 = RT.var((String)"datomic.memory-size", (String)"array-type");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.memory-size", (String)"handle-primitive-arrays");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"typesizes")))), RT.keyword(null, (String)"column"), 1});
        const__59 = RT.var((String)"clojure.core", (String)"extend");
        const__60 = RT.classForName((String)"java.lang.Object");
        const__61 = RT.classForName((String)"java.math.BigInteger");
        const__62 = RT.classForName((String)"java.lang.Double");
        const__63 = RT.classForName((String)"java.util.Date");
        const__64 = RT.classForName((String)"java.lang.String");
        const__65 = RT.classForName((String)"java.util.Map");
        const__66 = RT.classForName((String)"java.util.UUID");
        const__67 = RT.classForName((String)"java.util.Collection");
        const__68 = RT.classForName((String)"java.lang.Boolean");
        const__69 = RT.classForName((String)"java.lang.Integer");
        const__70 = RT.classForName((String)"java.net.URI");
        const__71 = RT.classForName((String)"java.lang.Short");
        const__72 = RT.classForName((String)"java.lang.Character");
        const__73 = RT.classForName((String)"clojure.lang.BigInt");
        const__74 = RT.classForName((String)"java.math.BigDecimal");
        const__75 = RT.classForName((String)"java.lang.Float");
        const__76 = RT.classForName((String)"clojure.lang.Keyword");
        const__77 = RT.classForName((String)"java.lang.Long");
        const__78 = RT.classForName((String)"clojure.lang.Symbol");
        const__79 = RT.classForName((String)"java.lang.Byte");
    }

    static {
        memory_size__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.memory_size__init").getClassLoader());
        try {
            memory_size__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

