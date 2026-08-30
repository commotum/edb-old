;;   Copyright (c) Nicola Mometto & contributors.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns clojure.tools.decompiler.source
  (:require [clojure.tools.decompiler.utils :refer [demunge]]))

(defmulti ast->clj :op)

(def primitive-array-descriptors
  {"boolean" "Z"
   "byte" "B"
   "char" "C"
   "double" "D"
   "float" "F"
   "int" "I"
   "long" "J"
   "short" "S"})

(def primitive-array-tags
  {"boolean[]" 'booleans
   "byte[]" 'bytes
   "char[]" 'chars
   "double[]" 'doubles
   "float[]" 'floats
   "int[]" 'ints
   "long[]" 'longs
   "short[]" 'shorts})

(defn type-tag [type]
  (if-let [tag (primitive-array-tags type)]
    tag
    (if (.endsWith ^String type "[]")
    (let [dimensions (loop [type type dimensions 0]
                       (if (.endsWith ^String type "[]")
                         (recur (subs type 0 (- (count type) 2)) (inc dimensions))
                         [type dimensions]))
          [component dimensions] dimensions]
      (str (apply str (repeat dimensions "["))
           (or (primitive-array-descriptors component)
               (str "L" component ";"))))
      (symbol type))))

(defmethod ast->clj :const [{:keys [val]}]
  val)

(defn case-test-form [test]
  (let [form (ast->clj test)]
    ;; Symbol constants arrive as `(quote sym)`, but case constants are data
    ;; and must be emitted as bare symbols.
    (if (and (seq? form)
             (= 'quote (first form))
             (symbol? (second form)))
      (second form)
      form)))

(defmethod ast->clj :case [{:keys [shift mask default type switch-type skip-check exprs test]}]
  `(case ~(ast->clj test)
     ~@(->> (for [[type match test expr] exprs]
              (if (= :collision type)
                (mapcat (fn [[test expr]]
                          [(case-test-form test) (ast->clj expr)])
                        test)
                [(case-test-form test) (ast->clj expr)]))
            (group-by second)
            (mapcat (fn [[then tests+thens]]
                      (let [tests (map first tests+thens)
                            test (if (= 1 (count tests))
                                   (first tests)
                                   tests)]
                        [test then]))))
     ~(ast->clj default)))

(defmethod ast->clj :monitor-enter [{:keys [sentinel]}]
  `(monitor-enter ~(ast->clj sentinel)))

(defmethod ast->clj :monitor-exit [{:keys [sentinel]}]
  `(monitor-exit ~(ast->clj sentinel)))

(defmethod ast->clj :do [{:keys [statements ret]}]
  `(do ~@(map ast->clj statements) ~(ast->clj ret)))

(defmethod ast->clj :local-variable [{:keys [local-variable init]}]
  `[~(ast->clj local-variable) ~(ast->clj init)])

(defmethod ast->clj :let [{:keys [local-variables body]}]
  `(let* [~@(mapcat ast->clj local-variables)] ~(ast->clj body)))

(defmethod ast->clj :letfn [{:keys [local-variables body]}]
  `(letfn* ~(->> (for [{:keys [local-variable init]} local-variables]
                   [(symbol (:name local-variable))
                    `(fn* ~(symbol (:name local-variable))
                          ~@(first (drop-while (complement sequential?) (ast->clj init))))])
                 (mapcat identity)
                 (vec))
           ~(ast->clj body)))

(defn hinted-symbol [{:keys [name type mutable?]}]
  (let [metadata (cond-> {}
                   (and type
                        (not= name "this")
                        (not= "java.lang.Object" type))
                   (assoc :tag (type-tag type))

                   (= :volatile-mutable mutable?)
                   (assoc :volatile-mutable true)

                   (= :unsynchronized-mutable mutable?)
                   (assoc :unsynchronized-mutable true))]
    (cond-> (symbol name)
      (seq metadata) (with-meta metadata))))

(defmethod ast->clj :method [{:keys [name return-type args body]}]
  `(~(hinted-symbol {:name (demunge name) :type return-type})
    [~@(map hinted-symbol args)]
    ~(ast->clj body)))

(defmethod ast->clj :reify [{:keys [interfaces methods]}]
  `(reify* ~(mapv symbol interfaces)
           ~@(map ast->clj methods)))

(defmethod ast->clj :import [{:keys [class]}]
  `(clojure.core/import* ~class))

;; WIP meta
(defmethod ast->clj :deftype [{:keys [name tname fields interfaces methods]}]
  `(deftype* ~(symbol tname) ~(symbol name)
     ~(mapv hinted-symbol fields)
     :implements ~(mapv symbol interfaces)
     ~@(map ast->clj methods)))

(defmethod ast->clj :loop [{:keys [local-variables body]}]
  `(loop* [~@(mapcat ast->clj local-variables)] ~(ast->clj body)))

(defmethod ast->clj :if [{:keys [test then else]}]
  `(if ~@(map ast->clj [test then else])))

(defmethod ast->clj :set! [{:keys [target val]}]
  `(set! ~(ast->clj target) ~(ast->clj val)))

(defmethod ast->clj :throw [{:keys [ex]}]
  `(throw ~(ast->clj ex)))

(defmethod ast->clj :new [{:keys [class args proxy-bases]}]
  (let [constructor (symbol (str class "."))
        constructor (cond-> constructor
                      (seq proxy-bases)
                      (with-meta {:proxy-bases (mapv symbol proxy-bases)}))]
    `(~constructor ~@(map ast->clj args))))

(defmethod ast->clj :instance-field [{:keys [instance field]}]
  `(~(demunge (str ".-" field)) ~(ast->clj instance)))

(defmethod ast->clj :local [{:keys [name cast]}]
  ;; A CHECKCAST on a local is often the only source-level evidence selecting
  ;; one Java overload over another (for example a Clojure fn implements both
  ;; Runnable and Callable).  The AST records the cast, so retain it as a type
  ;; hint instead of silently throwing it away during source emission.
  (hinted-symbol {:name name :type cast}))

(defmethod ast->clj :recur [{:keys [args]}]
  `(recur ~@(map ast->clj args)))

(defmethod ast->clj :vector [{:keys [items]}]
  (mapv ast->clj items))

(defmethod ast->clj :list [{:keys [items]}]
  `(list ~@(map ast->clj items)))

(defmethod ast->clj :set [{:keys [items]}]
  (into #{} (map ast->clj) items))

(defmethod ast->clj :map [{:keys [items]}]
  (into {} (map vec (partition 2 (map ast->clj items)))))

(defmethod ast->clj :array [{:keys [!items]}]
  (object-array (mapv ast->clj @!items)))

(defmethod ast->clj :fn [{:keys [fn-methods name]}]
  (if name
    `(fn* ~(symbol name) ~@(map ast->clj fn-methods))
    `(fn* ~@(map ast->clj fn-methods))))

(defmethod ast->clj :fn-method [{:keys [args body var-args? return-type]}]
  (let [argv (mapv hinted-symbol args)
        argv (if var-args?
               (let [rest-arg (peek argv)
                     rest-arg (with-meta rest-arg (dissoc (meta rest-arg) :tag))]
                 (into (pop argv) ['& rest-arg]))
               argv)
        argv (cond-> argv
               (and return-type
                    (not (#{"void" "java.lang.Object"} return-type)))
               (with-meta (assoc (meta argv) :tag (type-tag return-type))))]
    `(~argv ~(ast->clj body))))

(defmethod ast->clj :static-field [{:keys [target field]}]
  (symbol target field))

(defmethod ast->clj :invoke-static [{:keys [target method args]}]
  `(~(symbol target method) ~@(map ast->clj args)))

(defmethod ast->clj :invoke-instance [{:keys [target method args]}]
  `(~(demunge (str "." method)) ~(ast->clj target) ~@(map ast->clj args)))

(defmethod ast->clj :var [{:keys [ns name]}]
  (symbol ns name))

(defmethod ast->clj :the-var [{:keys [ns name]}]
  `(var ~(symbol ns name)))

(defmethod ast->clj :invoke [{:keys [fn args]}]
  `(~(ast->clj fn) ~@(map ast->clj args)))

(defmethod ast->clj :catch [{:keys [local body]}]
  `(catch ~(symbol (:type local)) ~(symbol (:name local)) ~(ast->clj body)))

(defmethod ast->clj :try [{:keys [body catches finally]}]
  `(try ~(ast->clj body)
        ~@(when catches
            (mapv ast->clj catches))
        ~@(when finally
            [`(finally ~(ast->clj finally))])))
