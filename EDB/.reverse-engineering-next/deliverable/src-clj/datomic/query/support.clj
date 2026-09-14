(do
  (clojure.core/in-ns 'datomic.query.support)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.edn :as 'edn])))
  (when-not (.equals 'datomic.query.support 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.query.support))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/require ['clojure.edn :as 'edn]))))
  (defn incorrect!
    ([msg]
      (throw
        (ex-info
          msg
          #:cognitect.anomalies{:category :cognitect.anomalies/incorrect, :message msg}))
      nil))
  (reset-meta!
    #'incorrect!
    (assoc
      {:private true, :arglists (clojure.core/list ['msg]), :column 1}
      :name
      'incorrect!
      :ns
      *ns*))
  (defn listq->mapq
    ([lq]
      (reduce
        (fn fn__19068
          ([m p__19067]
            (let [vec__19069 p__19067
                  k (nth vec__19069 (int 0) nil)
                  v (nth vec__19069 (int 1) nil)
                  k (first k)]
              (assoc m k v))))
        {}
        (partition 2 (partition-by #{:find :where :syms :keys :with :timeout :strs :in} lq)))))
  (defn disallow-find-variants!
    ([query]
      (when (some
              (fn fn__19075 ([p1__19074#] (or (vector? p1__19074#) (= '. p1__19074#))))
              (:find query))
        (incorrect! "Only find-rel elements are allowed in client :find"))))
  (defn query-map
    ([q] (let [q (if (string? q) (edn/read-string q) q)] (if (sequential? q) (listq->mapq q) q))))
  (defn parse-as
    ([q]
      (let [map__19080 (query-map q)
            map__19080 (if (seq? map__19080)
                         (clojure.lang.PersistentHashMap/create (seq map__19080))
                         map__19080)
            nq map__19080
            find (get map__19080 :find)
            keys (get map__19080 :keys)
            strs (get map__19080 :strs)
            syms (get map__19080 :syms)
            temp__5455__auto__ (or keys strs syms)]
        (if temp__5455__auto__
          (let [asyms temp__5455__auto__
                as (let [G__19081 asyms G__19081 (if keys (map keyword G__19081) G__19081)]
                     (cond-> (if strs (map str G__19081) G__19081) :then (vec)))]
            (when-not (= (count (or keys strs syms)) (count find))
              (throw (incorrect! "Count of :keys/:strs/:syms must match count of :find")))
            [(dissoc nq :keys :syms :strs) as])
          [nq nil]))))
  (defn counted-seq
    ([base_seq ct meta]
      (when-not (< ct 1)
        (proxy
          [clojure.lang.ASeq clojure.lang.Counted]
          [^clojure.lang.IPersistentMap meta]
          (more [] (rest base_seq))
          (seq [] base_seq)
          (next [] (next base_seq))
          (contains [o] (.contains ^java.util.List base_seq o))
          (count [] ct)
          (listIterator
            ([] (.listIterator ^java.util.List base_seq))
            ([index] (.listIterator ^java.util.List base_seq (int ^java.lang.Number index))))
          (cons [o] (cons o base_seq))
          (iterator [] (.iterator ^java.util.List base_seq))
          (subList
            [from to]
            (.subList
              ^java.util.List base_seq
              (int ^java.lang.Number from)
              (int ^java.lang.Number to)))
          (lastIndexOf
            [o]
            (java.lang.Integer/valueOf (int (.lastIndexOf ^java.util.List base_seq o))))
          (withMeta [meta] (counted-seq base_seq ct meta))
          (hashCode [] (java.lang.Integer/valueOf (int (.hashCode base_seq))))
          (hasheq [] (hash base_seq))
          (indexOf [o] (java.lang.Integer/valueOf (int (.indexOf ^java.util.List base_seq o))))
          (toArray ([] (.toArray ^java.util.List base_seq)) ([o] (.toArray base_seq o)))
          (get [index] (nth base_seq (int ^java.lang.Number index)))
          (equals [o] (.equals base_seq o))
          (equiv [o] (= base_seq o))
          (containsAll [c] (.containsAll ^java.util.List base_seq ^java.util.Collection c))
          (first [] (first base_seq)))))
    ([base_seq ct] (counted-seq base_seq ct nil))))