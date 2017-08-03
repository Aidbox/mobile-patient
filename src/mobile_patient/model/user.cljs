(ns mobile-patient.model.user
  (:require [clojure.spec.alpha :as s]))

(s/def ::id string?)

(s/def ::user-spec (s/keys :req-un [::id]))
