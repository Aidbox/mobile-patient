(ns mobile-patient.model.user
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)

(s/def ::user-spec (s/keys :req-un [::id]))

(defn get-official-name [domain-user]
  (->> domain-user
       :name
       (filter #(= (:use %) "official"))
       first
       :text))

(defn get-photo [domain-user]
  (get-in domain-user [:photo 0 :data]))
