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
import clojure.lang.Var;
import datomic.index$fn__15091$__GT_TreeIter__15176;
import java.util.Arrays;

public final class index$fn__15091
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"->TreeIter");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"lookup"), Symbol.intern(null, (String)"root"), Symbol.intern(null, (String)"ridx"), Symbol.intern(null, (String)"dir"), Symbol.intern(null, (String)"didx"), Symbol.intern(null, (String)"seg"), Symbol.intern(null, (String)"sidx")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.index.TreeIter");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.index.TreeIter"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new index$fn__15091$__GT_TreeIter__15176());
        return const__6;
    }

    public Object invoke() {
        return index$fn__15091.invokeStatic();
    }
}

