;; Copyright (c) Cognitect, Inc.
;; All rights reserved.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;      http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS-IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns datomic.query.support
  "Shared query-form normalization and result adapters. Accepts EDN strings, sequential query forms, and query maps; handles :keys, :strs, and :syms return-map specifications; preserves find order in indexed return maps; and provides counted lazy query results."
  (:require [clojure.edn :as edn]))

(defn- incorrect!
  [msg]
  (throw (ex-info msg {:cognitect.anomalies/category :cognitect.anomalies/incorrect
                       :cognitect.anomalies/message msg})))

(defn listq->mapq
  "Converts a sequential query form into a map keyed by :find, :with,
  :in, :where, :timeout, and return-map clauses. Clause values retain
  their original order."
  [lq]
  (->> lq
       (partition-by #{:find :keys :strs :syms :with :in :where :timeout})
       (partition 2)
       (reduce (fn [m [k v]]
                 (let [k (first k)]
                   (assoc m k v)))
               {})))

(defn disallow-find-variants!
  "Rejects scalar, collection, and tuple find specifications where only
  relation results are supported. Throws an incorrect-input anomaly."
  [query]
  (when (some #(or (vector? %) (= '. %)) (:find query))
    (incorrect! "Only find-rel elements are allowed in client :find")))

(defn query-map
  "Returns q as a query map. EDN strings are read first, sequential forms
  are partitioned into clauses, and maps are returned unchanged."
  [q]
  (let [q (if (string? q) (edn/read-string q) q)]
    (if (sequential? q)
      (listq->mapq q)
      q)))

(defn parse-as
  "Returns [query-map result-keys]. Converts :keys names to keywords,
  :strs names to strings, and :syms names to symbols, then removes the
  return-map clause from the query. The number of names must equal the
  number of :find elements."
  [q]
  (let [{:keys [find keys strs syms] :as nq} (query-map q)]
    (if-let [asyms (or keys strs syms)]
      (let [as (cond->> asyms
                        keys (map keyword)
                        strs (map str)
                        :then vec)]
        (when-not (= (count (or keys strs syms)) (count find))
          (throw (incorrect! "Count of :keys/:strs/:syms must match count of :find")))
        [(dissoc nq :keys :syms :strs) as])
      [nq nil])))

(defn counted-seq
  "Wraps base-seq in an ASeq whose Counted implementation returns ct
  without realizing the sequence. Returns nil when ct is less than one."
  ([base-seq ct]
     (counted-seq base-seq ct nil))
  ([^java.util.List base-seq ct meta]
     (if (< ct 1)
       nil
       (proxy [clojure.lang.ASeq clojure.lang.Counted] [meta]
         ;; ASeq impl
         (first [] (first base-seq))
         (next [] (next base-seq))
         (withMeta [meta] (counted-seq base-seq ct meta))

         ;; Counted
         (count [] ct)

         ;; ASeq overrides
         (equiv [o] (= base-seq o))
         (equals [o] (.equals base-seq o))
         (hashCode [] (.hashCode base-seq))
         (hasheq [] (hash base-seq))
         (seq [] base-seq)
         (cons [o] (cons o base-seq))
         (more [] (rest base-seq))
         (containsAll [c] (.containsAll base-seq c))
         (toArray ([] (.toArray base-seq))
                  ([o] (.toArray base-seq ^"[Ljava.lang.Object;" o)))
         (contains [o] (.contains base-seq o))
         (iterator [] (.iterator base-seq))
         (subList [from to] (.subList base-seq from to))
         (indexOf [o] (.indexOf base-seq o))
         (lastIndexOf [o] (.lastIndexOf base-seq o))
         (listIterator ([] (.listIterator base-seq))
                       ([index] (.listIterator base-seq index)))
         (get [index] (nth base-seq index))))))
