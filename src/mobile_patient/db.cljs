(ns mobile-patient.db
  (:require [clojure.spec.alpha :as s]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.model.patient :as patient-model]
            [mobile-patient.model.practitioner :as practitioner-model]
            ))

;; spec of app-db
(s/def ::user-id string?)
(s/def ::patient-id string?)

(s/def ::users
  (s/map-of string? ::user-model/user-spec))

(s/def ::patients
  (s/map-of string? ::patient-model/patient-spec))

(s/def ::practitioners
  (s/map-of string? ::practitioner-model/practitioner-spec))

(s/def ::app-db
  (s/keys :req-un [::user-id
                   ::patient-id
                   ::users
                   ::patients
                   ::practitioners
                   ]
          :opt-un []))

;; initial state of app-db
(def app-db {:spinner {}
             :current-screen :login
             :config {:base-url "https://sansara.health-samurai.io"
                      :client-id "sansara"}
             :active-medication-statements {}
             :other-medication-statements {}
             :observations {:status :not-asked}
             :users {}
             :chats []
             :messages []
             :user nil
             :patient-data {}
             :practitioner-data {}

             :user-id ""
             :patient-id ""
             :patients {}
             :practitioners {}
             })
