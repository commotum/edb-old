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
import datomic.fulltext$fn__14523$__GT_SearchIterator__14525;
import java.util.Arrays;

public final class fulltext$fn__14523
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fulltext", (String)"->SearchIterator");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"searcher"), (Object)Symbol.intern(null, (String)"search"), (Object)Symbol.intern(null, (String)"score-docs"), (Object)Symbol.intern(null, (String)"attr"), (Object)Symbol.intern(null, (String)"high-score")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.fulltext.SearchIterator");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.fulltext.SearchIterator"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new fulltext$fn__14523$__GT_SearchIterator__14525());
        return const__6;
    }

    public Object invoke() {
        return fulltext$fn__14523.invokeStatic();
    }
}

