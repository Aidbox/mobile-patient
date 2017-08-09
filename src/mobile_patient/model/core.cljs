(ns mobile-patient.model.core)

(defn list-to-map-by-id [items]
  (into (sorted-map)
        (map #(vector (:id %) %)
             items)))


(defn get-data-key [remote-data]
  (->> remote-data
       keys
       (map name)
       (filter #(clojure.string/ends-with? % "-data"))
       first
       keyword))
