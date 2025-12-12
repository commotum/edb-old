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

public final class integrity$loading__6434__auto____20643
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__2 = RT.keyword(null, (String)"exclude");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"assert")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 28}));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.pprint"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"pp"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"d"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"d"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.assert"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"assert")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 27})));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.btset"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"btset"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cache"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cache"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cli"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cli"));
    public static final AFn const__12 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cluster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cluster"));
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.clusterfs"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"clusterfs"));
    public static final AFn const__14 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"));
    public static final AFn const__15 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__16 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.coordination"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"coord"));
    public static final AFn const__17 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db"));
    public static final AFn const__18 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.domain"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"domain"));
    public static final AFn const__19 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.excise"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"excise"));
    public static final AFn const__20 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.fressian"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"fressian"));
    public static final AFn const__21 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.fulltext"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"fulltext"));
    public static final AFn const__22 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"index"));
    public static final AFn const__23 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.io"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"dio"));
    public static final AFn const__24 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.iter"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"iter"));
    public static final AFn const__25 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.kv-store"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"kvs"));
    public static final AFn const__26 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.log"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"log"));
    public static final AFn const__27 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.math"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"math"));
    public static final AFn const__28 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.memory-size"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"size"));
    public static final AFn const__29 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.peer"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"peer"));
    public static final AFn const__30 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.garbage"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"garbage"));
    public static final AFn const__31 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.uri"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"uri"));
    public static final AFn const__32 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.slf4j"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"logger"));
    public static final AFn const__33 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"stats"));
    public static final AFn const__34 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.tools"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"tools"));
    public static final AFn const__35 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.tools.index-checks"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"ic"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2, const__3);
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12, (Object)const__13, (Object)const__14, (Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__20, (Object)const__21, (Object)const__22, (Object)const__23, (Object)const__24, new Object[]{const__25, const__26, const__27, const__28, const__29, const__30, const__31, const__32, const__33, const__34, const__35});
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Database"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Datom"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.IDb"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.Attribute"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.index.RootNode"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.index.DirNode"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.fulltext.Root"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.clusterfs.ClusterFS"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.iter.Iter"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.btset.IDataSet"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Comparator"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

