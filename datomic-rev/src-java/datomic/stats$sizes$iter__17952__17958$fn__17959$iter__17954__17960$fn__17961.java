/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961$fn__17962;

public final class stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961
extends AFunction {
    Object iter__17954;
    Object db;
    Object s__17955;
    Object index;
    Object with_key_summary;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-buffer");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__12 = RT.keyword(null, (String)"fulltext");
    public static final Var const__13 = RT.var((String)"datomic.stats", (String)"key-summary");
    public static final AFn const__16 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"avet")});
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"merge-with");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"ns-resolve");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"datomic.stats");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"name");
    public static final Keyword const__26 = RT.keyword(null, (String)"index");
    public static final Keyword const__27 = RT.keyword(null, (String)"tier");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"rest");

    public stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.iter__17954 = object;
        this.db = object2;
        this.s__17955 = object3;
        this.index = object4;
        this.with_key_summary = object5;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__17969;
        Object s__17955;
        Object object2 = s__17955 = (this_.s__17955 = null);
        s__17955 = null;
        Object object3 = temp__5457__auto__17969 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961 this_;
            Object object4 = temp__5457__auto__17969;
            temp__5457__auto__17969 = null;
            Object s__179552 = object4;
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(s__179552);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object c__6023__auto__17966 = ((IFn)const__2.getRawRoot()).invoke(s__179552);
                int size__6024__auto__17967 = RT.intCast((int)RT.count((Object)c__6023__auto__17966));
                Object b__17957 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__17967);
                Object object6 = c__6023__auto__17966;
                c__6023__auto__17966 = null;
                Object object7 = ((IFn)new stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960$fn__17961$fn__17962(size__6024__auto__17967, this_.db, b__17957, object6, this_.index, this_.with_key_summary)).invoke();
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = b__17957;
                    b__17957 = null;
                    Object object9 = s__179552;
                    s__179552 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object8), ((IFn)this_.iter__17954).invoke(((IFn)const__8.getRawRoot()).invoke(object9)));
                } else {
                    Object object10 = b__17957;
                    b__17957 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10), null);
                }
            } else {
                Object object11;
                Object and__5236__auto__17968;
                Object tier = ((IFn)const__9.getRawRoot()).invoke(s__179552);
                IFn iFn = (IFn)const__10.getRawRoot();
                Object object12 = and__5236__auto__17968 = this_.with_key_summary;
                if (object12 != null && object12 != Boolean.FALSE) {
                    object11 = ((IFn)const__11.getRawRoot()).invoke((Object)const__12, this_.index);
                } else {
                    object11 = and__5236__auto__17968;
                    and__5236__auto__17968 = null;
                }
                Object ks = object11 != null && object11 != Boolean.FALSE ? ((IFn)const__13.getRawRoot()).invoke(((IFn)this_.index).invoke(((IFn)tier).invoke(this_.db)), ((IFn)const__16).invoke(this_.index)) : null;
                Object object13 = ((IFn)const__18.getRawRoot()).invoke(const__19.getRawRoot(), const__20.getRawRoot(), ((IFn)const__21.getRawRoot()).invoke(((IFn)((IFn)const__22.getRawRoot()).invoke((Object)const__23, ((IFn)const__24.getRawRoot()).invoke(((IFn)const__25.getRawRoot()).invoke(this_.index)))).invoke(this_.db, ((IFn)tier).invoke(this_.db))));
                Object[] objectArray = new Object[4];
                objectArray[0] = const__26;
                objectArray[1] = this_.index;
                objectArray[2] = const__27;
                Object object14 = tier;
                tier = null;
                objectArray[3] = object14;
                Object object15 = ks;
                ks = null;
                Object object16 = s__179552;
                s__179552 = null;
                this_ = null;
                object = iFn.invoke(((IFn)const__17.getRawRoot()).invoke(object13, (Object)RT.mapUniqueKeys((Object[])objectArray), object15), ((IFn)this_.iter__17954).invoke(((IFn)const__28.getRawRoot()).invoke(object16)));
            }
        } else {
            object = null;
        }
        return object;
    }
}

