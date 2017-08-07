(ns mobile-patient.model.practitioner
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)

(s/def ::photo vector?)

(s/def ::name vector?)

(s/def ::practitioner-spec (s/keys :req-un [::id ::photo ::name]))


(defn get-official-name [practitioner]
  (->> practitioner
       :name
       (filter #(= (:use %) "official"))
       first
       :text))

(defn get-photo [practitioner]
  (get-in practitioner [:photo 0 :data]))
