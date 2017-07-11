(ns mobile-patient.lib.helper
  (:require [clojure.string :as str]))

(defn contains-many? [m & ks]
  (every? #(contains? m %) ks))

(defn parms->query [parms]
  (if parms
    (str "?"
         (str/join
          "&"
          (map #(str
                 (js/encodeURIComponent (name (first %)))
                 "="
                 (js/encodeURIComponent (second %)))
               parms)))
    nil))

(defn query->params [str-params]
  "Get string of key=params separated by &
   Returns map of keys to params"
  (->> (str/split str-params #"[&=]")
       (map-indexed #(if (even? %1) (keyword %2) %2))
       (apply hash-map)))
