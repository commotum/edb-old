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
import datomic.Database;

public final class db$install_attribute_errors$fn__13084
extends AFunction {
    Object ebefore;
    Object after;
    Object eid;
    Object eafter;
    Object before;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__2 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"incompatible-schema-install");
    public static final Keyword const__4 = RT.keyword(null, (String)"entity");
    public static final Keyword const__5 = RT.keyword(null, (String)"attribute");
    public static final Keyword const__6 = RT.keyword(null, (String)"was");
    public static final Keyword const__7 = RT.keyword(null, (String)"requested");

    public db$install_attribute_errors$fn__13084(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.ebefore = object;
        this.after = object2;
        this.eid = object3;
        this.eafter = object4;
        this.before = object5;
    }

    public Object invoke(Object errs, Object k) {
        Object object;
        Object object2;
        Object and__5236__auto__13087;
        Object object3;
        Object or__5238__auto__13086;
        Object object4 = or__5238__auto__13086 = ((IFn)this_.ebefore).invoke(k);
        if (object4 != null && object4 != Boolean.FALSE) {
            object3 = or__5238__auto__13086;
            or__5238__auto__13086 = null;
        } else {
            object3 = ((IFn)this_.eafter).invoke(k);
        }
        Object object5 = and__5236__auto__13087 = object3;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.ebefore).invoke(k), ((IFn)this_.eafter).invoke(k));
        } else {
            object2 = and__5236__auto__13087;
            Object var3_3 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] objectArray = new Object[10];
            objectArray[0] = const__2;
            objectArray[1] = const__3;
            objectArray[2] = const__4;
            objectArray[3] = ((Database)this_.before).ident(this_.eid);
            objectArray[4] = const__5;
            objectArray[5] = k;
            objectArray[6] = const__6;
            objectArray[7] = ((Database)this_.before).ident(((IFn)this_.ebefore).invoke(k));
            objectArray[8] = const__7;
            Object object6 = k;
            k = null;
            objectArray[9] = ((Database)this_.after).ident(((IFn)this_.eafter).invoke(object6));
            Object object7 = errs;
            errs = null;
            db$install_attribute_errors$fn__13084 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object7);
        } else {
            object = errs;
            Object var1_1 = null;
        }
        return object;
    }
}

