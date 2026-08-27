/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.tools.index_checks$card_one_collisions$fn__21884;
import datomic.tools.index_checks$card_one_collisions$fn__21887;
import java.util.Arrays;

public final class index_checks$card_one_collisions
extends RestFn {
    public static final AFn const__2 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"eavt")});
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__5 = ((IObj)PersistentList.create(Arrays.asList(PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"eavt")}), Symbol.intern(null, (String)"sort")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__7 = RT.var((String)"datomic.tools", (String)"card-ones");
    public static final Var const__8 = RT.var((String)"datomic.tools", (String)"unsorted-seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__11 = RT.var((String)"datomic.api", (String)"datoms");

    public static Object invokeStatic(Object db2, Object sort, Object progress, ISeq components) {
        Object object = ((IFn)const__2).invoke(sort);
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__3.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__4.getRawRoot()).invoke(const__5))));
        }
        Object as = ((IFn)const__6.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__7.getRawRoot()).invoke(db2));
        Object object2 = progress;
        progress = null;
        Object object3 = as;
        as = null;
        Object object4 = db2;
        db2 = null;
        Object object5 = sort;
        sort = null;
        ISeq iSeq = components;
        components = null;
        return ((IFn)const__8.getRawRoot()).invoke((Object)new index_checks$card_one_collisions$fn__21884(object2), ((IFn)const__9.getRawRoot()).invoke((Object)new index_checks$card_one_collisions$fn__21887(object3), ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), object4, object5, (Object)iSeq)));
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return index_checks$card_one_collisions.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

