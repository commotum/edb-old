(do
  (clojure.core/in-ns 'datomic.tools.locate-transactor)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.api :as 'd]
        ['datomic.coordination :as 'coord]
        ['datomic.peer :as 'peer])))
  (when-not (.equals 'datomic.tools.locate-transactor 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.locate-transactor))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.api :as 'd]
          ['datomic.coordination :as 'coord]
          ['datomic.peer :as 'peer]))))
  (defn print-format
    ([endpoint]
      (update-in
        (dissoc endpoint :password)
        [:timestamp]
        (fn fn__20560 ([n] (when n (java.util.Date. (long ^java.lang.Number n))))))))
  (reset-meta!
    #'print-format
    (assoc
      {:arglists (clojure.core/list ['endpoint]), :column (int 1)}
      :name
      'print-format
      :ns
      *ns*))
  (defn -main
    ([uri]
      (try
        (loop [seq_20564 (seq coord/keys-by-role) chunk_20565 nil count_20566 0 i_20567 0]
          (if (< i_20567 count_20566)
            (let [vec__20568 (.nth ^clojure.lang.Indexed chunk_20565 (int i_20567))
                  role (nth vec__20568 (int 0) nil)
                  k (nth vec__20568 (int 1) nil)]
              (-> (peer/tx-group-endpoint uri k)
               (dissoc :password)
               (assoc :role role)
               (print-format)
               (pp/pprint))
              (recur seq_20564 chunk_20565 count_20566 (inc i_20567)))
            (let [temp__5825__auto__ (seq seq_20564)]
              (when temp__5825__auto__
                (let [seq_20564 temp__5825__auto__]
                  (if (chunked-seq? seq_20564)
                    (let [c__6090__auto__ (chunk-first seq_20564)]
                      (recur
                        (chunk-rest seq_20564)
                        c__6090__auto__
                        (int (count c__6090__auto__))
                        (int 0)))
                    (let [vec__20571 (first seq_20564)
                          role (nth vec__20571 (int 0) nil)
                          k (nth vec__20571 (int 1) nil)]
                      (-> (peer/tx-group-endpoint uri k)
                       (dissoc :password)
                       (assoc :role role)
                       (print-format)
                       (pp/pprint))
                      (recur (next seq_20564) nil 0 0))))))))
        (catch
          java.lang.Throwable
          t
          (do
            (.printStackTrace ^java.lang.Throwable t)
            (d/shutdown true)
            (java.lang.System/exit (int -1))
            nil)))
      (d/shutdown true)
      (java.lang.System/exit (int 0))
      nil))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name '-main :ns *ns*)))