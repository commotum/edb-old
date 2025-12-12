/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.lang.reflect.Field;

public final class datafy$_datafy_declared_fields_fn$fn__20545
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"this");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"symbol");

    public Object invoke(Object f) {
        Object object = f;
        f = null;
        String name = ((Field)object).getName();
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)name);
        String string = name;
        name = null;
        return Tuple.create((Object)object2, (Object)((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)const__3, ((IFn)const__4.getRawRoot()).invoke((Object)string)));
    }
}

