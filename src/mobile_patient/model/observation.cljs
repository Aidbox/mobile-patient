(ns mobile-patient.model.observation
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)
(s/def ::status string?)
(s/def ::subject map?)

(s/def ::observation-spec (s/keys :req-un [::id ::status ::subject]))
