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
package datomic.core2.atom;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.core2.atom.logged$fn__19778$__GT_LoggedAtom__20179;
import java.util.Arrays;

public final class logged$fn__19778
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.atom.logged", (String)"->LoggedAtom");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"log"), Symbol.intern(null, (String)"close-ch"), Symbol.intern(null, (String)"state-ref"), Symbol.intern(null, (String)"serialize"), Symbol.intern(null, (String)"deserialize"), Symbol.intern(null, (String)"validator-ref"), Symbol.intern(null, (String)"watches-ref")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.core2.atom.logged.LoggedAtom");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.core2.atom.logged.LoggedAtom"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new logged$fn__19778$__GT_LoggedAtom__20179());
        return const__6;
    }

    public Object invoke() {
        return logged$fn__19778.invokeStatic();
    }
}

