(ns mobile-patient.model.patient)

(defn get-official-name [patient]
  (->> patient
       :name
       (filter #(= (:use %) "official"))
       first
       :text))

(defn get-photo [patient]
  (get-in patient [:photo 0 :data]))
