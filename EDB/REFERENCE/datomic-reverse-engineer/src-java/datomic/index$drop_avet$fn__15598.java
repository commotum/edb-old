/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$drop_avet$fn__15598$fn__15603;
import datomic.index.DirNode;
import datomic.index.RootNode;

public final class index$drop_avet$fn__15598
extends AFunction {
    Object aid;
    Object oldroot;
    int ct;
    Object olookup;
    Object store;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"*pace-index-fn*");
    public static final Object const__1 = 1L;
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"<=");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__8 = RT.var((String)"datomic.index", (String)"drop-dirnode-leaves");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__12 = RT.keyword(null, (String)"keydata");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__15 = RT.keyword(null, (String)"dirids");
    public static final Var const__16 = RT.var((String)"datomic.index", (String)"write-object");
    public static final Keyword const__17 = RT.keyword(null, (String)"garbage");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Keyword const__21 = RT.keyword(null, (String)"default");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"pr-str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public index$drop_avet$fn__15598(Object object, Object object2, int n, Object object3, Object object4) {
        this.aid = object;
        this.oldroot = object2;
        this.ct = n;
        this.olookup = object3;
        this.store = object4;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Object invoke(Object result2, Object n) {
        Object object;
        index$drop_avet$fn__15598 this_;
        Object object2;
        Object and__5236__auto__15608;
        Object object3;
        Object olddirid;
        Object object4;
        Object temp__5457__auto__15607;
        Object object5 = temp__5457__auto__15607 = const__0.get();
        if (object5 != null && object5 != Boolean.FALSE) {
            Object f;
            Object object6 = temp__5457__auto__15607;
            temp__5457__auto__15607 = null;
            Object object7 = f = object6;
            f = null;
            ((IFn)object7).invoke(const__1);
        }
        Object kd = RT.nth((Object)((RootNode)this_.oldroot).keydata, (int)RT.uncheckedIntCast((Object)((Number)n)));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object8 = kd;
        Object object9 = iLookupThunk.get(object8);
        if (iLookupThunk == object9) {
            __thunk__0__ = __site__0__.fault(object8);
            object9 = __thunk__0__.get(object8);
        }
        Object a1 = object9;
        Number n2 = Numbers.unchecked_inc((Object)n);
        if (Numbers.lt((Object)n2, (long)this_.ct)) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Number number = n2;
            n2 = null;
            Object object10 = RT.nth((Object)((RootNode)this_.oldroot).keydata, (int)RT.uncheckedIntCast((Object)number));
            object4 = iLookupThunk2.get(object10);
            if (iLookupThunk2 == object4) {
                __thunk__1__ = __site__1__.fault(object10);
                object4 = __thunk__1__.get(object10);
            }
        } else {
            object4 = Numbers.num((long)Long.MAX_VALUE);
        }
        Number a2 = object4;
        Object object11 = a1;
        a1 = null;
        Number number = a2;
        a2 = null;
        Object object12 = ((IFn)const__6.getRawRoot()).invoke(object11, this_.aid, (Object)number);
        Object object13 = olddirid = object12 != null && object12 != Boolean.FALSE ? RT.nth((Object)((RootNode)this_.oldroot).dirids, (int)RT.uncheckedIntCast((Object)((Number)n))) : null;
        Object olddir = object13 != null && object13 != Boolean.FALSE ? ((IFn)const__7.getRawRoot()).invoke(this_.olookup, olddirid) : null;
        Object object14 = olddirid;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = olddir;
            olddir = null;
            object3 = ((IFn)const__8.getRawRoot()).invoke(object15, (Object)new index$drop_avet$fn__15598$fn__15603(this_.aid));
        } else {
            object3 = null;
        }
        Object vec__15599 = object3;
        Object newdir = RT.nth(vec__15599, (int)RT.uncheckedIntCast((long)0L), null);
        Object object16 = vec__15599;
        vec__15599 = null;
        Object leaf_garbage = RT.nth((Object)object16, (int)RT.uncheckedIntCast((long)1L), null);
        Object object17 = and__5236__auto__15608 = newdir;
        if (object17 != null && object17 != Boolean.FALSE) {
            object2 = ((IFn)const__10.getRawRoot()).invoke(leaf_garbage);
        } else {
            object2 = and__5236__auto__15608;
            and__5236__auto__15608 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object18 = result2;
            result2 = null;
            Object object19 = newdir;
            newdir = null;
            Object object20 = leaf_garbage;
            leaf_garbage = null;
            Object object21 = olddirid;
            olddirid = null;
            this_ = null;
            object = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object18, (Object)const__12, const__13.getRawRoot(), ((IFn)const__14.getRawRoot()).invoke(((DirNode)newdir).keydata)), (Object)const__15, const__13.getRawRoot(), ((IFn)const__16.getRawRoot()).invoke(this_.store, this_.olookup, object19)), (Object)const__17, const__18.getRawRoot(), object20), (Object)const__17, const__13.getRawRoot(), object21);
            return object;
        } else {
            Object object22;
            Object and__5236__auto__15609;
            Object object23 = and__5236__auto__15609 = ((IFn)const__19.getRawRoot()).invoke(newdir);
            if (object23 != null && object23 != Boolean.FALSE) {
                object22 = ((IFn)const__10.getRawRoot()).invoke(leaf_garbage);
            } else {
                object22 = and__5236__auto__15609;
                and__5236__auto__15609 = null;
            }
            if (object22 != null && object22 != Boolean.FALSE) {
                Object object24 = result2;
                result2 = null;
                Object object25 = leaf_garbage;
                leaf_garbage = null;
                Object object26 = olddirid;
                olddirid = null;
                this_ = null;
                object = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object24, (Object)const__17, const__18.getRawRoot(), object25), (Object)const__17, const__13.getRawRoot(), object26);
                return object;
            } else {
                Object object27;
                Object or__5238__auto__15611;
                Object object28 = olddirid;
                olddirid = null;
                Object object29 = or__5238__auto__15611 = ((IFn)const__19.getRawRoot()).invoke(object28);
                if (object29 != null && object29 != Boolean.FALSE) {
                    object27 = or__5238__auto__15611;
                    or__5238__auto__15611 = null;
                } else {
                    Object and__5236__auto__15610;
                    Object object30 = newdir;
                    newdir = null;
                    Object object31 = and__5236__auto__15610 = object30;
                    if (object31 != null && object31 != Boolean.FALSE) {
                        Object object32 = leaf_garbage;
                        leaf_garbage = null;
                        object27 = ((IFn)const__20.getRawRoot()).invoke(object32);
                    } else {
                        object27 = and__5236__auto__15610;
                        and__5236__auto__15610 = null;
                    }
                }
                if (object27 != null && object27 != Boolean.FALSE) {
                    Object object33 = result2;
                    result2 = null;
                    Object object34 = kd;
                    kd = null;
                    Object object35 = n;
                    n = null;
                    this_ = null;
                    object = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object33, (Object)const__12, const__13.getRawRoot(), object34), (Object)const__15, const__13.getRawRoot(), RT.nth((Object)((RootNode)this_.oldroot).dirids, (int)RT.uncheckedIntCast((Object)((Number)object35))));
                    return object;
                } else {
                    Keyword keyword = const__21;
                    if (keyword == null) return null;
                    if (keyword == Boolean.FALSE) return null;
                    Boolean bl = Boolean.FALSE;
                    if (bl == null) throw (Throwable)((Object)new AssertionError(((IFn)const__22.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__23.getRawRoot()).invoke((Object)Boolean.FALSE))));
                    if (bl == Boolean.FALSE) throw (Throwable)((Object)new AssertionError(((IFn)const__22.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__23.getRawRoot()).invoke((Object)Boolean.FALSE))));
                    return null;
                }
            }
        }
    }
}

