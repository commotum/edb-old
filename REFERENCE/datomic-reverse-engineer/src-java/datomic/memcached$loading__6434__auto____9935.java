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
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class memcached$loading__6434__auto____9935
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"with-nano-time")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 47})));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config-ext"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cfext"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.monitor"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"monitor"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.require"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"require"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cache.impl"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cache-impl"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.measure.io-stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io-stats"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.lang.AutoCloseable"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.nio.ByteBuffer"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.MemcachedClient"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.AddrUtil"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.KetamaConnectionFactory"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.ConnectionFactoryBuilder"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.ConnectionFactoryBuilder$Locator"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.ConnectionFactoryBuilder$Protocol"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.FailureMode"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.DefaultHashAlgorithm"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.OperationTimeoutException"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.CachedData"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.ConnectionFactory"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.internal.OperationCompletionListener"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.internal.OperationFuture"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.transcoders.Transcoder"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.auth.AuthDescriptor"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.spy.memcached.auth.PlainCallbackHandler"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.net.InetSocketAddress"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.net.InetAddress"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.ExecutionException"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.Future"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.Semaphore"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.TimeUnit"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

