(do
  (clojure.core/in-ns 'datomic.memory-size)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.memory-size 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.memory-size))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (set! *warn-on-reflection* true)
  (defonce MemorySize {})
  (defprotocol MemorySize (memory-size [_]))
  (def REFERENCE 8)
  (reset-meta! #'REFERENCE (assoc {:const true, :column 1} :name 'REFERENCE :ns *ns*))
  (def LONG 8)
  (reset-meta! #'LONG (assoc {:const true, :column 1} :name 'LONG :ns *ns*))
  (def INT 4)
  (reset-meta! #'INT (assoc {:const true, :column 1} :name 'INT :ns *ns*))
  (def CHAR 2)
  (reset-meta! #'CHAR (assoc {:const true, :column 1} :name 'CHAR :ns *ns*))
  (def ARRAY 16)
  (reset-meta! #'ARRAY (assoc {:const true, :column 1} :name 'ARRAY :ns *ns*))
  (def STRING 56)
  (reset-meta! #'STRING (assoc {:const true, :column 1} :name 'STRING :ns *ns*))
  (defn array-type
    ([t] (.getClass (java.lang.reflect.Array/newInstance ^java.lang.Class t (int 0)))))
  (defn handle-primitive-arrays
    ([&form &env & typesizes]
      (seq
        (concat
          (clojure.core/list 'do)
          (map
            (fn fn__400
              ([p__399]
                (let [vec__401 p__399
                      type (nth vec__401 (int 0) nil)
                      size (nth vec__401 (int 1) nil)]
                  (seq
                    (concat
                      (clojure.core/list 'clojure.core/extend-type)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'datomic.memory-size/array-type)
                            (clojure.core/list type))))
                      (clojure.core/list 'datomic.memory-size/MemorySize)
                      (clojure.core/list
                        (seq
                          (concat
                            (clojure.core/list 'memory-size)
                            (clojure.core/list
                              (apply vector (seq (concat (clojure.core/list 'x)))))
                            (clojure.core/list
                              (seq
                                (concat
                                  (clojure.core/list 'clojure.core/+)
                                  (clojure.core/list 16)
                                  (clojure.core/list
                                    (seq
                                      (concat
                                        (clojure.core/list 'clojure.core/*)
                                        (clojure.core/list size)
                                        (clojure.core/list
                                          (seq
                                            (concat
                                              (clojure.core/list 'clojure.core/count)
                                              (clojure.core/list 'x))))))))))))))))))
            (partition 2 typesizes))))))
  (.setMacro #'handle-primitive-arrays)
  (extend
    (array-type java.lang.Long/TYPE)
    MemorySize
    {:memory-size (fn fn__406 ([x] (long (+ 16 (* 8 (count x))))))})
  (extend
    (array-type java.lang.Integer/TYPE)
    MemorySize
    {:memory-size (fn fn__408 ([x] (long (+ 16 (* 4 (count x))))))})
  (extend
    (array-type java.lang.Boolean/TYPE)
    MemorySize
    {:memory-size (fn fn__410 ([x] (long (+ 16 (* 1 (count x))))))})
  (extend
    (array-type java.lang.Double/TYPE)
    MemorySize
    {:memory-size (fn fn__412 ([x] (long (+ 16 (* 8 (count x))))))})
  (extend
    (array-type java.lang.Float/TYPE)
    MemorySize
    {:memory-size (fn fn__414 ([x] (long (+ 16 (* 4 (count x))))))})
  (extend
    (array-type java.lang.Short/TYPE)
    MemorySize
    {:memory-size (fn fn__416 ([x] (long (+ 16 (* 2 (count x))))))})
  (extend
    (array-type java.lang.Character/TYPE)
    MemorySize
    {:memory-size (fn fn__418 ([x] (long (+ 16 (* 2 (count x))))))})
  (extend
    (array-type java.lang.Byte/TYPE)
    MemorySize
    {:memory-size (fn fn__420 ([x] (long (+ 16 (* 1 (count x))))))})
  (extend nil MemorySize {:memory-size (fn fn__422 ([_] 0))})
  (extend
    java.lang.Object
    MemorySize
    {:memory-size
     (fn fn__424
       ([o]
         (let [cls (.getClass o)]
           (if (.isArray ^java.lang.Class cls)
             (do
               (let [a o
                     a__6200__auto__ a
                     l__6201__auto__ (alength ^"[Ljava.lang.Object;" a__6200__auto__)]
                 (loop [idx 0 ret 16]
                   (if (< idx l__6201__auto__)
                     (recur
                       (inc (int idx))
                       (+
                         ret
                         (max 8 (long (memory-size (aget ^"[Ljava.lang.Object;" a (int idx)))))))
                     (long ret))))
               nil)
             (if (not (identical? cls java.lang.Object))
               (do (throw (java.lang.Error. (str "No size estimator for " cls))) nil)
               16)))))})
  (extend java.math.BigInteger MemorySize {:memory-size (fn fn__428 ([_] 60))})
  (extend java.lang.Double MemorySize {:memory-size (fn fn__430 ([o] 24))})
  (extend java.util.Date MemorySize {:memory-size (fn fn__432 ([date] 32))})
  (extend
    java.lang.String
    MemorySize
    {:memory-size (fn fn__434 ([s] (long (+ 56 (* 2 (.length ^java.lang.String s))))))})
  (extend
    java.util.Map
    MemorySize
    {:memory-size
     (fn fn__436
       ([m]
         (reduce
           (fn fn__438
             ([s p__437]
               (let [vec__439 p__437 k (nth vec__439 (int 0) nil) v (nth vec__439 (int 1) nil)]
                 (+ (+ (+ s 36) (memory-size k)) (memory-size v)))))
           0
           m)))})
  (extend java.util.UUID MemorySize {:memory-size (fn fn__444 ([uuid] 64))})
  (extend
    java.util.Collection
    MemorySize
    {:memory-size
     (fn fn__446 ([c] (reduce (fn fn__447 ([s v] (+ (+ s 24) (memory-size v)))) 0 c)))})
  (extend java.lang.Boolean MemorySize {:memory-size (fn fn__450 ([o] 24))})
  (extend java.lang.Integer MemorySize {:memory-size (fn fn__452 ([o] 24))})
  (extend
    java.net.URI
    MemorySize
    {:memory-size
     (fn fn__454
       ([uri]
         (long
           (+
             (-> (+ 16 32)
              (+ 160)
              (+ 48)
              (+ 8)
              (+ (* 2 (count (.getSchemeSpecificPart ^java.net.URI uri)))))
             (* 2 (count (.getScheme ^java.net.URI uri)))))))})
  (extend java.lang.Short MemorySize {:memory-size (fn fn__456 ([o] 24))})
  (extend java.lang.Character MemorySize {:memory-size (fn fn__458 ([o] 24))})
  (extend
    clojure.lang.BigInt
    MemorySize
    {:memory-size (fn fn__460 ([bi] (+ 16 (memory-size (.-bipart ^clojure.lang.BigInt bi)))))})
  (extend
    java.math.BigDecimal
    MemorySize
    {:memory-size
     (fn fn__462 ([bd] (+ 32 (memory-size (.unscaledValue ^java.math.BigDecimal bd)))))})
  (extend java.lang.Float MemorySize {:memory-size (fn fn__464 ([o] 24))})
  (extend clojure.lang.Keyword MemorySize {:memory-size (fn fn__466 ([kw] 48))})
  (extend java.lang.Long MemorySize {:memory-size (fn fn__468 ([o] 24))})
  (extend
    clojure.lang.Symbol
    MemorySize
    {:memory-size (fn fn__470 ([s] (+ 48 (memory-size (meta s)))))})
  (extend java.lang.Byte MemorySize {:memory-size (fn fn__472 ([o] 24))}))