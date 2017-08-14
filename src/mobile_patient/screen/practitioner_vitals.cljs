(ns mobile-patient.screen.practitioner-vitals
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.component.medications :refer [medications-component]]
            [mobile-patient.component.patients :refer [Patients]]
            [goog.object :refer [getValueByKeys]]))


(defn on-press-callback [item navigation]
  (rf/dispatch-sync [:set-current-patient (:id item)])
  (rf/dispatch-sync [:do-load-vitals-sign])
  (navigation.navigate "Vitals" #js{:patient-name (str (user-model/get-official-name item) " Vitals")}))


(defn PractitionerVitalsScreen [{:keys [navigation]}]
  (let [patients (rf/subscribe [:patients])]
    (fn []
      (ui/show-remote-data
       @patients
       (fn [data]
         [Patients (vals data) navigation on-press-callback "chevron-right"]
         )))))
