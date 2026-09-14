/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960;

public final class stats$sizes$iter__17952__17958$fn__17959
extends AFunction {
    Object iter__17952;
    Object s__17953;
    Object db;
    Object with_key_summary;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__5 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"index"), (Object)RT.keyword(null, (String)"mid-index"), (Object)RT.keyword(null, (String)"history"));
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"rest");

    public stats$sizes$iter__17952__17958$fn__17959(Object object, Object object2, Object object3, Object object4) {
        this.iter__17952 = object;
        this.s__17953 = object2;
        this.db = object3;
        this.with_key_summary = object4;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__17953 = this_.s__17953 = null;
            while (true) {
                Object fs__6022__auto__17973;
                stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960 iterys__6021__auto__17972;
                Object index2;
                Object xs__6012__auto__17974;
                Object temp__5457__auto__17975;
                Object object2 = temp__5457__auto__17975 = ((IFn)const__0.getRawRoot()).invoke(s__17953);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__17975;
                temp__5457__auto__17975 = null;
                Object object4 = xs__6012__auto__17974 = object3;
                xs__6012__auto__17974 = null;
                Object object5 = index2 = ((IFn)const__1.getRawRoot()).invoke(object4);
                index2 = null;
                stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960 stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960 = iterys__6021__auto__17972 = new stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960(this_.db, object5, this_.with_key_summary);
                iterys__6021__auto__17972 = null;
                Object object6 = fs__6022__auto__17973 = ((IFn)const__0.getRawRoot()).invoke(((IFn)stats$sizes$iter__17952__17958$fn__17959$iter__17954__17960).invoke((Object)const__5));
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = fs__6022__auto__17973;
                    fs__6022__auto__17973 = null;
                    Object object8 = s__17953;
                    s__17953 = null;
                    stats$sizes$iter__17952__17958$fn__17959 this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(object7, ((IFn)this_.iter__17952).invoke(((IFn)const__7.getRawRoot()).invoke(object8)));
                    break block2;
                }
                Object object9 = s__17953;
                s__17953 = null;
                s__17953 = ((IFn)const__7.getRawRoot()).invoke(object9);
            }
            object = null;
        }
        return object;
    }
}

