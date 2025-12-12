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

public final class tools$loading__6434__auto____21721
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.java.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io"));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.pprint"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"pp"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"d"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db-io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db-io"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cache"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cache"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.catalog"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"catalog"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"));
    public static final AFn const__12 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.coordination"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"coord"));
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.domain"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"domain"));
    public static final AFn const__14 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"index"));
    public static final AFn const__15 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.iter"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"iter"));
    public static final AFn const__16 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.kv-cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"kvc"));
    public static final AFn const__17 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.log"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"log"));
    public static final AFn const__18 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.math"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"math"));
    public static final AFn const__19 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.memory-size"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"size"));
    public static final AFn const__20 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.monitor"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"monitor"));
    public static final AFn const__21 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.peer"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"peer"));
    public static final AFn const__22 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.require"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"req"));
    public static final AFn const__23 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));
    public static final AFn const__24 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"stats"));
    public static final AFn const__25 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.uri"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"uri"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12, (Object)const__13, (Object)const__14, (Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__20, (Object)const__21, (Object)const__22, new Object[]{const__23, const__24, const__25});
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.nio.ByteBuffer"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Datom"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

