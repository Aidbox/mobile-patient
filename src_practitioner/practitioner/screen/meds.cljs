(ns practitioner.screen.meds
  (:require [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.component.medications :refer [medications-component]]
            [goog.object :refer [getValueByKeys]]))


(defn MedsScreen [{:keys [navigation]}]
  (let [t1 (new js/Date)
        loading (rf/subscribe [:get-in [:spinner :load-patient-data]])
        user-id (getValueByKeys navigation "state" "params" "patientid")
        ;;patient-ref @(rf/subscribe [:patient-ref])
        patient-ref (rf/subscribe [:get-patient-ref-by-id user-id])
        active (rf/subscribe [:active-medication-statements @patient-ref])
        other (rf/subscribe [:other-medication-statements @patient-ref])]
    (fn []
      (if (or @loading (< (- (new js/Date) t1) 300))
        [ui/activity-indicator]
        [medications-component {:active active
                                :other other}]
        ))))
