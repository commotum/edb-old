/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;
import datomic.index$dropped_avet_aids$fadd__15588;

public final class index$dropped_avet_aids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__3 = RT.keyword(null, (String)"aevt");
    public static final AFn const__5 = (AFn)Tuple.create((Object)RT.keyword((String)"db", (String)"index"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword((String)"db", (String)"unique"));

    public static Object invokeStatic(Object db2) {
        Database hist = ((Database)db2).history();
        Object object = db2;
        db2 = null;
        index$dropped_avet_aids$fadd__15588 fadd = new index$dropped_avet_aids$fadd__15588(object);
        IFn iFn = (IFn)fadd;
        index$dropped_avet_aids$fadd__15588 index$dropped_avet_aids$fadd__15588 = fadd;
        fadd = null;
        Object object2 = ((IFn)index$dropped_avet_aids$fadd__15588).invoke(((IFn)const__1.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY), ((IFn)const__2.getRawRoot()).invoke((Object)hist, (Object)const__3, (Object)const__5));
        Database database = hist;
        hist = null;
        return ((IFn)const__0.getRawRoot()).invoke(iFn.invoke(object2, ((IFn)const__2.getRawRoot()).invoke((Object)database, (Object)const__3, (Object)const__7)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$dropped_avet_aids.invokeStatic(object2);
    }
}

