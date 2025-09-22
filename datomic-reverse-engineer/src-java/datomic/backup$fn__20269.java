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
import datomic.backup$fn__20269$__GT_SegSetStorage__20287;
import datomic.backup$fn__20269$map__GT_SegSetStorage__20289;
import java.util.Arrays;

public final class backup$fn__20269
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"->SegSetStorage");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"declared"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
    public static final Var const__5 = RT.var((String)"datomic.backup", (String)"map->SegSetStorage");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"declared"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
    public static final AFn const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"storage"), (Object)Symbol.intern(null, (String)"seg-id-set")))), RT.keyword(null, (String)"column"), 1});
    public static final AFn const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m__7585__auto__")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__12 = RT.classForName((String)"datomic.backup.SegSetStorage");

    public static Object invokeStatic() {
        const__0.setMeta((IPersistentMap)const__4);
        const__5.setMeta((IPersistentMap)const__6);
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.backup.SegSetStorage"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__9);
        var.bindRoot((Object)new backup$fn__20269$__GT_SegSetStorage__20287());
        Var var2 = const__5;
        var2.setMeta((IPersistentMap)const__11);
        var2.bindRoot((Object)new backup$fn__20269$map__GT_SegSetStorage__20289());
        return const__12;
    }

    public Object invoke() {
        return backup$fn__20269.invokeStatic();
    }
}

