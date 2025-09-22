/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.impl.db.IDatum;

public final class db$alter_attribute_hook
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"validate-hook-target");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"installed-attribute?");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-alter-attribute");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"alter-attribute");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__12 = RT.keyword((String)"db", (String)"errors");

    public static Object invokeStatic(Object before, Object after, Object d, Object check_QMARK_) {
        Object object;
        Object and__5236__auto__13234;
        Object object2 = check_QMARK_;
        if (object2 != null && object2 != Boolean.FALSE) {
            ((IFn)const__0.getRawRoot()).invoke(after, d);
            Object eid = ((IDatum)d).getV();
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(before, eid);
            if (object3 != null && object3 != Boolean.FALSE) {
            } else {
                Object object4 = eid;
                eid = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"Cannot alter attribute that does not exist: ", ((IFn)const__5.getRawRoot()).invoke(before, object4)));
            }
        }
        Object object5 = before;
        before = null;
        Object object6 = after;
        after = null;
        Object object7 = d;
        d = null;
        Object vec__13230 = ((IFn)const__6.getRawRoot()).invoke(object5, object6, object7);
        Object db2 = RT.nth((Object)vec__13230, (int)RT.uncheckedIntCast((long)0L), null);
        Object object8 = vec__13230;
        vec__13230 = null;
        Object errors = RT.nth((Object)object8, (int)RT.uncheckedIntCast((long)1L), null);
        Object object9 = check_QMARK_;
        check_QMARK_ = null;
        Object object10 = and__5236__auto__13234 = object9;
        if (object10 != null && object10 != Boolean.FALSE) {
            object = ((IFn)const__10.getRawRoot()).invoke(errors);
        } else {
            object = and__5236__auto__13234;
            and__5236__auto__13234 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object11 = ((IFn)const__4.getRawRoot()).invoke((Object)"Error: ", ((IFn)const__11.getRawRoot()).invoke(errors));
            Object[] objectArray = new Object[2];
            objectArray[0] = const__12;
            Object object12 = errors;
            errors = null;
            objectArray[1] = object12;
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, object11, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        Object object13 = db2;
        db2 = null;
        return object13;
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
        return db$alter_attribute_hook.invokeStatic(object5, object6, object7, object8);
    }
}

