/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class query$process_ranges$fn__19335
extends AFunction {
    Object norm;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__6 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"="), Symbol.intern(null, (String)"<"), Symbol.intern(null, (String)"<="), Symbol.intern(null, (String)">"), Symbol.intern(null, (String)">=")});
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"ffirst");

    public query$process_ranges$fn__19335(Object object) {
        this.norm = object;
    }

    public Object invoke(Object p1__19320_SHARP_) {
        Boolean bl;
        Boolean or__5238__auto__19341;
        Object object;
        boolean and__5236__auto__19340 = p1__19320_SHARP_ instanceof List;
        if (and__5236__auto__19340) {
            boolean and__5236__auto__19339 = ((IFn)const__2.getRawRoot()).invoke(p1__19320_SHARP_) instanceof List;
            if (and__5236__auto__19339) {
                boolean and__5236__auto__19338 = Util.equiv((long)RT.count((Object)p1__19320_SHARP_), (long)1L);
                if (and__5236__auto__19338) {
                    Object and__5236__auto__19337;
                    Object object2 = and__5236__auto__19337 = ((IFn)const__6).invoke(((IFn)const__7.getRawRoot()).invoke(p1__19320_SHARP_));
                    if (object2 != null && object2 != Boolean.FALSE) {
                        Object object3 = p1__19320_SHARP_;
                        p1__19320_SHARP_ = null;
                        object = ((IFn)this.norm).invoke(object3);
                    } else {
                        object = and__5236__auto__19337;
                        and__5236__auto__19337 = null;
                    }
                } else {
                    object = and__5236__auto__19338 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__19339 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object = and__5236__auto__19340 ? Boolean.TRUE : Boolean.FALSE;
        }
        Boolean bl2 = or__5238__auto__19341 = object;
        if (bl2 != null && bl2 != Boolean.FALSE) {
            bl = or__5238__auto__19341;
            or__5238__auto__19341 = null;
        } else {
            bl = null;
        }
        return bl;
    }
}

