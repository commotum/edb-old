/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class pull$try_xform$fn__18947
extends AFunction {
    Object f;
    Object xform;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"cancelled?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"ex-data");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"eval-exception");
    public static final Keyword const__3 = RT.keyword(null, (String)"context");
    public static final Keyword const__4 = RT.keyword(null, (String)"xform");
    public static final Keyword const__5 = RT.keyword(null, (String)"expr");
    public static final Keyword const__6 = RT.keyword(null, (String)"arguments");

    public pull$try_xform$fn__18947(Object object, Object object2) {
        this.f = object;
        this.xform = object2;
    }

    public Object invoke(Object v) {
        Object object;
        try {
            object = ((IFn)this.f).invoke(v);
        }
        catch (Throwable t2) {
            Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)t2));
            if (object2 != null && object2 != Boolean.FALSE) {
                Object t2 = null;
                throw t2;
            }
            Object[] objectArray = new Object[6];
            objectArray[0] = const__3;
            objectArray[1] = const__4;
            objectArray[2] = const__5;
            objectArray[3] = this.xform;
            objectArray[4] = const__6;
            Object object3 = v;
            v = null;
            objectArray[5] = Tuple.create((Object)object3);
            Object t2 = null;
            throw (Throwable)((IFn)const__2.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), (Object)t2);
        }
        return object;
    }
}

