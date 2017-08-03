(ns mobile-patient.model.patient
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)

(s/def ::photo vector?)

(s/def ::name vector?)

(s/def ::patient-spec (s/keys :req-un [::id ::photo ::name]))


(defn get-official-name [patient]
  (->> patient
       :name
       (filter #(= (:use %) "official"))
       first
       :text))

(defn get-photo [patient]
  (get-in patient [:photo 0 :data]))
