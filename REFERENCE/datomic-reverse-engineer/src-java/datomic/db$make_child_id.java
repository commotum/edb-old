/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class db$make_child_id
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"has-unique-id?");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"tempid");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"invalid-nested-entity");
    public static final Keyword const__6 = RT.keyword(null, (String)"entity");

    public static Object invokeStatic(Object db2, Object parentid, Object attrid, Object childmap) {
        Object object;
        Object object2;
        Object or__5238__auto__13720;
        Object attr;
        Object object3 = attrid;
        attrid = null;
        Object object4 = attr = ((IFn)const__0.getRawRoot()).invoke(db2, object3);
        attr = null;
        Object object5 = or__5238__auto__13720 = ((Attribute)object4).isComponent;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = or__5238__auto__13720;
            or__5238__auto__13720 = null;
        } else {
            Object object6 = db2;
            db2 = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke(object6, childmap);
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn.LO)const__2.getRawRoot()).invokePrim(16L);
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__6;
            Object object7 = childmap;
            childmap = null;
            objectArray[1] = object7;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)"Nested entity is not a component and has no :db/id", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$make_child_id.invokeStatic(object5, object6, object7, object8);
    }
}

