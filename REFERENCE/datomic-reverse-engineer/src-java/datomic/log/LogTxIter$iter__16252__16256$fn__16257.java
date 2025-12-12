/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogTxIter$iter__16252__16256$fn__16257$fn__16258;

public final class LogTxIter$iter__16252__16256$fn__16257
extends AFunction {
    Object iter__16252;
    Object root_val;
    Object lookup;
    Object s__16253;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-buffer");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__11 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"rest");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public LogTxIter$iter__16252__16256$fn__16257(Object object, Object object2, Object object3, Object object4) {
        this.iter__16252 = object;
        this.root_val = object2;
        this.lookup = object3;
        this.s__16253 = object4;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__16263;
        Object s__16253;
        Object object2 = s__16253 = (this_.s__16253 = null);
        s__16253 = null;
        Object object3 = temp__5457__auto__16263 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            LogTxIter$iter__16252__16256$fn__16257 this_;
            Object object4 = temp__5457__auto__16263;
            temp__5457__auto__16263 = null;
            Object s__162532 = object4;
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(s__162532);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object c__6023__auto__16261 = ((IFn)const__2.getRawRoot()).invoke(s__162532);
                int size__6024__auto__16262 = RT.intCast((int)RT.count((Object)c__6023__auto__16261));
                Object b__16255 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__16262);
                Object object6 = c__6023__auto__16261;
                c__6023__auto__16261 = null;
                Object object7 = ((IFn)new LogTxIter$iter__16252__16256$fn__16257$fn__16258(b__16255, this_.root_val, this_.lookup, size__6024__auto__16262, object6)).invoke();
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = b__16255;
                    b__16255 = null;
                    Object object9 = s__162532;
                    s__162532 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object8), ((IFn)this_.iter__16252).invoke(((IFn)const__8.getRawRoot()).invoke(object9)));
                } else {
                    Object object10 = b__16255;
                    b__16255 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10), null);
                }
            } else {
                Object ri = ((IFn)const__9.getRawRoot()).invoke(s__162532);
                IFn iFn = (IFn)const__10.getRawRoot();
                IFn iFn2 = (IFn)const__11.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object11 = ri;
                ri = null;
                Object object12 = RT.nth((Object)this_.root_val, (int)RT.intCast((Object)((Number)object11)));
                Object object13 = iLookupThunk.get(object12);
                if (iLookupThunk == object13) {
                    __thunk__0__ = __site__0__.fault(object12);
                    object13 = __thunk__0__.get(object12);
                }
                Object object14 = s__162532;
                s__162532 = null;
                this_ = null;
                object = iFn.invoke(iFn2.invoke(this_.lookup, object13), ((IFn)this_.iter__16252).invoke(((IFn)const__14.getRawRoot()).invoke(object14)));
            }
        } else {
            object = null;
        }
        return object;
    }
}

