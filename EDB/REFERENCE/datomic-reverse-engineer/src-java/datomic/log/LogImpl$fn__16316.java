/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class LogImpl$fn__16316
extends AFunction {
    Object bufs;
    Object desc;
    Object new_tail;
    Object log;
    Object cs;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"write-tail-descriptor");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__4 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"rev"));
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"inc");
    public static final Var const__6 = RT.var((String)"datomic.io", (String)"unchunk");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"tail");
    public static final Keyword const__9 = RT.keyword(null, (String)"desc");
    public static final Keyword const__10 = RT.keyword(null, (String)"threw");

    public LogImpl$fn__16316(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.bufs = object;
        this.desc = object2;
        this.new_tail = object3;
        this.log = object4;
        this.cs = object5;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object temp__5455__auto__16318;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.cs = null;
            this.bufs = null;
            Object object = temp__5455__auto__16318 = ((IFn)const__1.getRawRoot()).invoke(this.cs, ((IFn)const__2.getRawRoot()).invoke(this.desc, (Object)const__4, const__5.getRawRoot()), ((IFn)const__6.getRawRoot()).invoke(this.bufs));
            if (object == null || object == Boolean.FALSE) {
                throw (Throwable)new Error("Conflict updating log tail");
            }
            Object object2 = temp__5455__auto__16318;
            temp__5455__auto__16318 = null;
            Object new_desc = object2;
            this.new_tail = null;
            Object object3 = new_desc;
            new_desc = null;
            objectArray[1] = ((IFn)const__7.getRawRoot()).invoke(this.log, (Object)const__8, this.new_tail, (Object)const__9, object3);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__10;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

