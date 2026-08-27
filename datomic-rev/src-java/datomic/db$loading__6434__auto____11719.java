/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Associative;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class db$loading__6434__auto____11719
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__2 = RT.keyword(null, (String)"exclude");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"resolve"), (Object)Symbol.intern(null, (String)"compare"), (Object)Symbol.intern(null, (String)"qualified-symbol?"));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"use");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"datomic.btset");
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"only"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"compare")));
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.set"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"set"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.pprint"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"pp"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.string"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"str"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.edn"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"edn"));
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"datomic.validators");
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"qualified-symbol?")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 47})));
    public static final AFn const__14 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__15 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.thread"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"thread"));
    public static final AFn const__16 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.math"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"math"));
    public static final AFn const__17 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.measure.io-stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io-stats"));
    public static final AFn const__18 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.measure.io-trace"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io-trace"));
    public static final AFn const__19 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io"));
    public static final AFn const__20 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.iter"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"iter"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"iget"), Symbol.intern(null, (String)"inext")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 43})));
    public static final AFn const__21 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.fressian"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"fressian"));
    public static final AFn const__22 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.janino"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"janino"));
    public static final AFn const__23 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.monitor"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"monitor"));
    public static final AFn const__24 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__25 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));
    public static final AFn const__26 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.fulltext-index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"ftindex"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2, (Object)const__3);
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)const__6);
            ((IFn)const__7.getRawRoot()).invoke((Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12, (Object)const__13, (Object)const__14, (Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__20, (Object)const__21, (Object)const__22, (Object)const__23, (Object)const__24, (Object)const__25, (Object)const__26);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Database"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Datom"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Database$Predicate"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.btset.IDataSet"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.iter.Iter"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.impl.Circular"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.impl.db.IDatum"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Comparator"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.ArrayList"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.HashMap"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.HashSet"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Map"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Map$Entry"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Date"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.UUID"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.LinkedBlockingQueue"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.TimeUnit"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.ThreadPoolExecutor"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.Executor"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.atomic.LongAdder"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.atomic.AtomicBoolean"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.net.URI"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"org.fressian.handlers.WriteHandlerLookup"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"org.fressian.handlers.IWriteHandlerLookup"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

