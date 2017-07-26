(ns mobile-patient.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::greeting]))

;; initial state of app-db
(def app-db {:spinner {}
             :current-screen :login
             :config {:base-url "https://sansara.health-samurai.io"
                      :client-id "sansara"}
             :active-medication-statements {}
             :other-medication-statements {}
             :observations {:status :not-asked}
             :contacts []
             :chats []
             :messages []
             :resource-type-data {}
             :practitioner-patients {}
             :user nil
             :patient-data {}
             :practitioner-data {}
             })
