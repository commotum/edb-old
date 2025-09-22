/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.Namespace
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
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class peer$loading__6434__auto____20733
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.java.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"jio"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.pprint"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"pprint"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.string"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"str"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.adopter"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"adopter"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cache"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cache"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cleanup"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cleanup"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster-stack"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster-stack"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"));
    public static final AFn const__12 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db"));
    public static final AFn const__14 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db-io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db-io"));
    public static final AFn const__15 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.domain"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"domain"));
    public static final AFn const__16 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"index"));
    public static final AFn const__17 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io"));
    public static final AFn const__18 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.log"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"log"));
    public static final AFn const__19 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.math"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"math"));
    public static final AFn const__20 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.memory"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"memory"));
    public static final AFn const__21 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.monitor"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"monitor"));
    public static final AFn const__22 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.promise"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"promise"));
    public static final AFn const__23 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.queue"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"queue"));
    public static final AFn const__24 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.reconnector2"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"recon"));
    public static final AFn const__25 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.transaction"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"tx"));
    public static final AFn const__26 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));
    public static final AFn const__27 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.uri"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"uri"));
    public static final AFn const__28 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__29 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.coordination"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"coord"));
    public static final AFn const__30 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.connector"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"conn"));
    public static final AFn const__31 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.require"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"req"));
    public static final AFn const__32 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.catalog"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"catalog"));
    public static final AFn const__33 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.fulltext-index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"ftindex"));
    public static final AFn const__34 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cast2slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cast2slf4j"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12, (Object)const__13, (Object)const__14, (Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__20, (Object)const__21, (Object)const__22, new Object[]{const__23, const__24, const__25, const__26, const__27, const__28, const__29, const__30, const__31, const__32, const__33, const__34});
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Database"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.impl.db.IDatum"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.IDbImpl"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.lang.ref.WeakReference"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.net.URI"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Map"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Queue"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.ArrayBlockingQueue"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.concurrent.LinkedBlockingQueue"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

