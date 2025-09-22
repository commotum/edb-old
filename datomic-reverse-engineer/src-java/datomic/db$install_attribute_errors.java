/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$install_attribute_errors$fn__13080;
import datomic.db$install_attribute_errors$fn__13082;
import datomic.db$install_attribute_errors$fn__13084;
import datomic.db$install_attribute_errors$fn__13088;

public final class db$install_attribute_errors
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Keyword const__1 = RT.keyword(null, (String)"raw");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"required-schema-attrs");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"tuple-install-errors");
    public static final Keyword const__9 = RT.keyword((String)"db", (String)"unique");
    public static final Keyword const__12 = RT.keyword((String)"db", (String)"valueType");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__14 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"unique-not-allowed");
    public static final Keyword const__16 = RT.keyword(null, (String)"entity");
    public static final Keyword const__17 = RT.keyword((String)"db", (String)"isComponent");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__19 = 20L;
    public static final Keyword const__20 = RT.keyword((String)"db.error", (String)"components-must-be-refs");
    public static final Var const__21 = RT.var((String)"datomic.db", (String)"installed-attribute?");
    public static final Var const__22 = RT.var((String)"datomic.db", (String)"functional-attr-ids");

    public static Object invokeStatic(Object before, Object after, Object eid) {
        Object object;
        Object object2;
        Object and__5236__auto__13093;
        Object object3;
        Object object4;
        Object and__5236__auto__13092;
        Object object5;
        Object object6;
        Object and__5236__auto__13091;
        Object errors;
        Object eafter = ((IFn)const__0.getRawRoot()).invoke(after, eid, (Object)const__1, (Object)Boolean.TRUE);
        Object object7 = errors = ((IFn)const__2.getRawRoot()).invoke((Object)new db$install_attribute_errors$fn__13080(eafter), null, ((IFn)const__3.getRawRoot()).invoke((Object)new db$install_attribute_errors$fn__13082(after), const__4.getRawRoot()));
        errors = null;
        Object errors2 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(after, eafter), object7));
        Object object8 = and__5236__auto__13091 = RT.get((Object)eafter, (Object)const__9);
        if (object8 != null && object8 != Boolean.FALSE) {
            object6 = Util.equiv((long)27L, (Object)RT.get((Object)eafter, (Object)const__12)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object6 = and__5236__auto__13091;
            and__5236__auto__13091 = null;
        }
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object9 = errors2;
            errors2 = null;
            object5 = ((IFn)const__13.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__14, const__15, const__16, eafter}), object9);
        } else {
            object5 = errors2;
            errors2 = null;
        }
        Object errors3 = object5;
        Object object10 = and__5236__auto__13092 = RT.get((Object)eafter, (Object)const__17);
        if (object10 != null && object10 != Boolean.FALSE) {
            object4 = ((IFn)const__18.getRawRoot()).invoke(const__19, RT.get((Object)eafter, (Object)const__12));
        } else {
            object4 = and__5236__auto__13092;
            and__5236__auto__13092 = null;
        }
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object11 = errors3;
            errors3 = null;
            object3 = ((IFn)const__13.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__14, const__20, const__16, eafter}), object11);
        } else {
            object3 = errors3;
            errors3 = null;
        }
        Object errors4 = object3;
        Object object12 = and__5236__auto__13093 = before;
        if (object12 != null && object12 != Boolean.FALSE) {
            object2 = ((IFn)const__21.getRawRoot()).invoke(before, eid);
        } else {
            object2 = and__5236__auto__13093;
            and__5236__auto__13093 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object ebefore;
            Object object13 = ebefore = ((IFn)const__0.getRawRoot()).invoke(before, eid, (Object)const__1, (Object)Boolean.TRUE);
            ebefore = null;
            Object object14 = after;
            after = null;
            Object object15 = eid;
            eid = null;
            Object object16 = eafter;
            eafter = null;
            db$install_attribute_errors$fn__13084 db$install_attribute_errors$fn__13084 = new db$install_attribute_errors$fn__13084(object13, object14, object15, object16, before);
            Object object17 = errors4;
            errors4 = null;
            db$install_attribute_errors$fn__13088 db$install_attribute_errors$fn__13088 = new db$install_attribute_errors$fn__13088(before);
            Object object18 = before;
            before = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)db$install_attribute_errors$fn__13084, object17, ((IFn)const__3.getRawRoot()).invoke((Object)db$install_attribute_errors$fn__13088, ((IFn)const__22.getRawRoot()).invoke(object18)));
        } else {
            object = errors4;
            errors4 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$install_attribute_errors.invokeStatic(object4, object5, object6);
    }
}

