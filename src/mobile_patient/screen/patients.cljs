(ns mobile-patient.screen.patients
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.model.patient :as patient-model]
            [mobile-patient.component.patients :refer [Patients]]))

(defn on-press-callback []
  )


(defn PatientsScreen [{:keys [navigation]}]
  (let [patients @(rf/subscribe [:patients])]
    (ui/show-remote-data
     patients
     (fn [data]
       [Patients data navigation on-press-callback "add"]))))
