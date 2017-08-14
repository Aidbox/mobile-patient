(ns mobile-patient.screen.meds
  (:require [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.component.medications :refer [medications-component]]
            [goog.object :refer [getValueByKeys]]))

(defn MedsScreen [{:keys [navigation]}]
  (let [patient-id (rf/subscribe [:patient-id])
        active (rf/subscribe [:active-medication-statements @patient-id])
        other (rf/subscribe [:other-medication-statements @patient-id])]
    (fn []
      [medications-component {:active active
                              :other other}])))
