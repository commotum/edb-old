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

public final class query$loading__6434__auto____17823
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__2 = RT.keyword(null, (String)"exclude");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"compare"), (Object)Symbol.intern(null, (String)"qualified-symbol?"));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.db"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"db"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.cache"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cache"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.index"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"index"));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.iter"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"iter"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.fulltext"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"ft"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.config"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.common"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"common"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"compare"), Symbol.intern(null, (String)"qualified-symbol?")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 38})));
    public static final AFn const__12 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.datalog"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"datalog"));
    public static final AFn const__13 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__14 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.pull"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"pull"));
    public static final AFn const__15 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.query.support"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"qs"));
    public static final AFn const__16 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.measure.query-stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"query-stats"));
    public static final AFn const__17 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.measure.io-stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io-stats"));
    public static final AFn const__18 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.set"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"set"));
    public static final AFn const__19 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.pprint"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"pp"));
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"datomic.extensions");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"datomic.aggregation");

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2, (Object)const__3);
            ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11, (Object)const__12, (Object)const__13, (Object)const__14, (Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__20, (Object)const__21);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Map"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"clojure.lang.IPersistentVector"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Database"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.Entity"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.IDbImpl"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.IDb"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.Attribute"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.Db"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.impl.db.IDatum"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.btset.IDataSet"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.List"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Set"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Comparator"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.ArrayList"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.util.Collection"));
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.iter.Iter"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

