/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
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
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$fn__13421$__GT_Db__13564;
import datomic.db$fn__13421$map__GT_Db__13566;
import java.util.Arrays;

public final class db$fn__13421
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"->Db");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"declared"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"map->Db");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"declared"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
    public static final AFn const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"id"), Symbol.intern(null, (String)"memidx"), Symbol.intern(null, (String)"indexing"), Symbol.intern(null, (String)"mid-index"), Symbol.intern(null, (String)"index"), Symbol.intern(null, (String)"history"), Symbol.intern(null, (String)"memlog"), Symbol.intern(null, (String)"basisT"), Symbol.intern(null, (String)"nextT"), Symbol.intern(null, (String)"indexBasisT"), Symbol.intern(null, (String)"indexingNextT"), Symbol.intern(null, (String)"elements"), Symbol.intern(null, (String)"keys"), Symbol.intern(null, (String)"ids"), Symbol.intern(null, (String)"index-root-id"), Symbol.intern(null, (String)"index-rev"), Symbol.intern(null, (String)"asOfT"), Symbol.intern(null, (String)"sinceT"), Symbol.intern(null, (String)"raw"), Symbol.intern(null, (String)"filt")}))), RT.keyword(null, (String)"column"), 1});
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m__7585__auto__")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__12 = RT.classForName((String)"datomic.db.Db");

    public static Object invokeStatic() {
        const__0.setMeta((IPersistentMap)const__4);
        const__5.setMeta((IPersistentMap)const__6);
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.db.Db"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__9);
        var.bindRoot((Object)new db$fn__13421$__GT_Db__13564());
        Var var2 = const__5;
        var2.setMeta((IPersistentMap)const__11);
        var2.bindRoot((Object)new db$fn__13421$map__GT_Db__13566());
        return const__12;
    }

    public Object invoke() {
        return db$fn__13421.invokeStatic();
    }
}

