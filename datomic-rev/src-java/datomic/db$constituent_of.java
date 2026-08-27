/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.impl.Circular;
import java.util.Arrays;

public final class db$constituent_of
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"supports-tuples?");
    public static final AFn const__1 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"find"), Tuple.create((Object)Symbol.intern(null, (String)"?ident"), (Object)Symbol.intern(null, (String)"...")), RT.keyword(null, (String)"in"), Symbol.intern(null, (String)"$"), Symbol.intern(null, (String)"?aid"), RT.keyword(null, (String)"where"), Tuple.create((Object)Symbol.intern(null, (String)"?aid"), (Object)RT.keyword((String)"db", (String)"ident"), (Object)Symbol.intern(null, (String)"?attr")), Tuple.create((Object)Symbol.intern(null, (String)"?comp"), (Object)RT.keyword((String)"db", (String)"tupleAttrs"), (Object)Symbol.intern(null, (String)"?attrs")), Tuple.create((Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"set"), Symbol.intern(null, (String)"?attrs")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 20})), (Object)Symbol.intern(null, (String)"?attr-set")), Tuple.create((Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"contains?"), Symbol.intern(null, (String)"?attr-set"), Symbol.intern(null, (String)"?attr")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 20}))), Tuple.create((Object)Symbol.intern(null, (String)"?comp"), (Object)RT.keyword((String)"db", (String)"ident"), (Object)Symbol.intern(null, (String)"?ident"))});

    public static Object invokeStatic(Object db2, Object aid) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(db2);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = aid;
            aid = null;
            object = Circular.q(const__1, Tuple.create((Object)object3, (Object)object4));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$constituent_of.invokeStatic(object3, object4);
    }
}

