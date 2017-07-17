(ns patient.screen.meds
  (:require [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.component.medications :refer [medications-component]]
            [goog.object :refer [getValueByKeys]]))


(defn MedsScreen [{:keys [navigation]}]
  (let [;;user-id (getValueByKeys navigation "state" "params" "patientid")
        ;;patient-ref @(rf/subscribe [:patient-ref])
        patient-ref (rf/subscribe [:patient-ref])
        active (rf/subscribe [:active-medication-statements @patient-ref])
        other (rf/subscribe [:other-medication-statements @patient-ref])]
    (fn []
      [medications-component {:active active
                              :other other}])))
