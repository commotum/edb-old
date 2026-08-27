/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogDir;
import datomic.log.LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274$fn__16275;

public final class LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274
extends AFunction {
    Object lookup;
    Object iter__16267;
    Object s__16268;
    Object dir;
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
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"rest");

    public LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274(Object object, Object object2, Object object3, Object object4) {
        this.lookup = object;
        this.iter__16267 = object2;
        this.s__16268 = object3;
        this.dir = object4;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__16280;
        Object s__16268;
        Object object2 = s__16268 = (this_.s__16268 = null);
        s__16268 = null;
        Object object3 = temp__5457__auto__16280 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274 this_;
            Object object4 = temp__5457__auto__16280;
            temp__5457__auto__16280 = null;
            Object s__162682 = object4;
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(s__162682);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object c__6023__auto__16278 = ((IFn)const__2.getRawRoot()).invoke(s__162682);
                int size__6024__auto__16279 = RT.intCast((int)RT.count((Object)c__6023__auto__16278));
                Object b__16270 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__16279);
                Object object6 = c__6023__auto__16278;
                c__6023__auto__16278 = null;
                Object object7 = ((IFn)new LogTxIter$iter__16265__16271$fn__16272$iter__16267__16273$fn__16274$fn__16275(b__16270, size__6024__auto__16279, object6, this_.lookup, this_.dir)).invoke();
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = b__16270;
                    b__16270 = null;
                    Object object9 = s__162682;
                    s__162682 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object8), ((IFn)this_.iter__16267).invoke(((IFn)const__8.getRawRoot()).invoke(object9)));
                } else {
                    Object object10 = b__16270;
                    b__16270 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10), null);
                }
            } else {
                Object di;
                Object object11 = di = ((IFn)const__9.getRawRoot()).invoke(s__162682);
                di = null;
                Object object12 = s__162682;
                s__162682 = null;
                this_ = null;
                object = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(this_.lookup, ((LogDir)RT.nth((Object)this_.dir, (int)RT.intCast((Object)((Number)object11)))).uuid), ((IFn)this_.iter__16267).invoke(((IFn)const__13.getRawRoot()).invoke(object12)));
            }
        } else {
            object = null;
        }
        return object;
    }
}

