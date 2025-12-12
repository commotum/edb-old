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

public final class db$find_alter_fn$fn__13213
extends AFunction {
    Object to;
    Object from;
    Object fid;
    Object eid;
    public static final Keyword const__0 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"unsupported-alter-schema");
    public static final Keyword const__2 = RT.keyword(null, (String)"entity");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Keyword const__4 = RT.keyword(null, (String)"attribute");
    public static final Keyword const__5 = RT.keyword(null, (String)"from");
    public static final Keyword const__6 = RT.keyword(null, (String)"to");

    public db$find_alter_fn$fn__13213(Object object, Object object2, Object object3, Object object4) {
        this.to = object;
        this.from = object2;
        this.fid = object3;
        this.eid = object4;
    }

    public Object invoke(Object db2, Object _, Object _2, Object _3) {
        Object object = db2;
        Object[] objectArray = new Object[10];
        objectArray[0] = const__0;
        objectArray[1] = const__1;
        objectArray[2] = const__2;
        objectArray[3] = ((IFn)const__3.getRawRoot()).invoke(db2, this.eid);
        objectArray[4] = const__4;
        objectArray[5] = ((IFn)const__3.getRawRoot()).invoke(db2, this.fid);
        objectArray[6] = const__5;
        objectArray[7] = ((IFn)const__3.getRawRoot()).invoke(db2, this.from);
        objectArray[8] = const__6;
        Object object2 = db2;
        db2 = null;
        objectArray[9] = ((IFn)const__3.getRawRoot()).invoke(object2, this.to);
        return Tuple.create((Object)object, (Object)Tuple.create((Object)RT.mapUniqueKeys((Object[])objectArray)));
    }
}

