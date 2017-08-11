(ns mobile-patient.model.chat
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)
(s/def ::resourceType string?)
(s/def ::participants vector?)
(s/def ::name string?)

(s/def ::chat-spec (s/keys :req-un [::id
                                    ::resourceType
                                    ::participants
                                    ::name]))

(defn get-participants-set [chat]
  (->> chat
       :participants
       (filter #(= "User" (:resourceType %)))
       (map :id)
       set))

(defn other-participant-id [chat domain-user]
  {:post [(string? %)]}
  (let [participants (->> (:participants chat) (map :id) set)]
    (first (disj participants (:id domain-user)))))

