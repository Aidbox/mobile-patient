(ns mobile-patient.db
  (:require [clojure.spec.alpha :as s]
            [mobile-patient.model.user :as user-model]))

;; spec of app-db
(s/def ::user-id string?)

(s/def ::users
  (s/map-of string? ::user-model/user-spec ))

(s/def ::app-db
  (s/keys :req-un [::user-id
                   ::users]))

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
             :practitioner-patients {}
             :user nil
             :patient-data {}
             :practitioner-data {}

             :user-id ""
             :patients {}
             :patient-id ""
             })
