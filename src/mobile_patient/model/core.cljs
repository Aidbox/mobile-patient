(ns mobile-patient.model.core)

(defn list-to-map-by-id [items]
  (into (sorted-map)
        (map #(vector (:id %) %)
             items)))
