/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import org.fressian.Reader;

public final class transaction$read_message
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.transaction", (String)"reader");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"put");
    public static final Var const__2 = RT.var((String)"datomic.transaction", (String)"log-event-map");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"getx");
    public static final Keyword const__4 = RT.keyword(null, (String)"id");
    public static final Keyword const__5 = RT.keyword(null, (String)"read-at");

    public static Object invokeStatic(Object is) {
        Object fin;
        long read_at = System.nanoTime();
        Object object = is;
        is = null;
        Object object2 = fin = ((IFn)const__0.getRawRoot()).invoke(object);
        fin = null;
        Object msg = ((Reader)object2).readObject();
        ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(msg, (Object)const__4), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__5, Numbers.num((long)read_at)}));
        Object object3 = msg;
        msg = null;
        return object3;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return transaction$read_message.invokeStatic(object2);
    }
}

