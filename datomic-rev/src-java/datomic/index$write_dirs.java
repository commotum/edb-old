/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class index$write_dirs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"<!!");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__5 = RT.var((String)"datomic.index", (String)"write-vals");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");

    public static Object invokeStatic(Object cstore, Object olookup, Object ch) {
        Object rootnode = PersistentVector.EMPTY;
        while (true) {
            Object temp__5455__auto__15422;
            Object object = temp__5455__auto__15422 = ((IFn)const__0.getRawRoot()).invoke(ch);
            if (object == null || object == Boolean.FALSE) break;
            Object object2 = temp__5455__auto__15422;
            temp__5455__auto__15422 = null;
            Object vec__15418 = object2;
            Object key = RT.nth((Object)vec__15418, (int)RT.uncheckedIntCast((long)0L), null);
            Object object3 = vec__15418;
            vec__15418 = null;
            Object dbuf = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
            Object dirid = ((IFn)const__4.getRawRoot()).invoke();
            Object[] objectArray = new Object[2];
            objectArray[0] = dirid;
            Object object4 = dbuf;
            dbuf = null;
            objectArray[1] = object4;
            ((IFn)const__5.getRawRoot()).invoke(cstore, (Object)RT.mapUniqueKeys((Object[])objectArray));
            PersistentVector persistentVector = rootnode;
            rootnode = null;
            Object object5 = key;
            key = null;
            Object object6 = dirid;
            dirid = null;
            rootnode = ((IFn)const__6.getRawRoot()).invoke((Object)persistentVector, (Object)Tuple.create((Object)object5, (Object)object6));
        }
        Object var3_3 = null;
        return rootnode;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$write_dirs.invokeStatic(object4, object5, object6);
    }
}

