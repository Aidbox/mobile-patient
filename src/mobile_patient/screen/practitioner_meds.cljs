(ns mobile-patient.screen.practitioner-meds
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.component.medications :refer [medications-component]]
            [mobile-patient.component.patients :refer [Patients]]
            [goog.object :refer [getValueByKeys]]))


(defn on-press-callback [item navigation]
  (rf/dispatch-sync [:set-current-patient (:id item)])
  (rf/dispatch [:do-load-medication-statements])
  (navigation.navigate "Meds" #js{:patient-name (str (user-model/get-official-name item) " Meds")}))


(defn PractitionerMedsScreen [{:keys [navigation]}]
  (let [patients (rf/subscribe [:patients])]
    (fn []
      (ui/show-remote-data
       @patients
       (fn [data]
         [Patients (vals data) navigation on-press-callback "chevron-right"]
         )))))
