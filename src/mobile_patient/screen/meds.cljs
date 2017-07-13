(ns mobile-patient.screen.meds
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [goog.object :refer [getValueByKeys]]))

(defn medication [med-st]
  (or (-> med-st :medication :CodeableConcept :coding first :display)
      (get-in med-st [:medication :CodeableConcept :text])))

(defn section-header [text]
  [ui/view {:style {:margin 0
                    :padding 10
                    :padding-top 20
                    :padding-left 25
                    :background-color :white}}
   [ui/text {:style {:color color/pink
                     :font-size 14
                     :font-weight :bold
                     }}
    text]])

(defn section-row [{:keys [title description on-press]}]
  [ui/view {:style {:border-bottom-width 1
                    :border-color "#ddd"
                    :background-color :white
                    :padding 15
                    :padding-left 25}}
   [ui/text {:style {:color :black
                     :font-weight :bold
                     :font-size 15
                     }}
    title]
   [ui/text {:style {:color "#aaa"
                     :font-size 14
                     :padding-right 80
                     :line-height 22}}
    description]])

(defn section [{:keys [title]} & childs]
  [ui/view
   [section-header title]
   [ui/view childs]])

(defn MedsScreen [{:keys [navigation]}]
  (let [t1 (new js/Date)
        loading (rf/subscribe [:get-in [:spinner :load-patient-data]])
        user-id (getValueByKeys navigation "state" "params" "patientid")
        ;;patient-ref @(rf/subscribe [:patient-ref])
        patient-ref (rf/subscribe [:get-patient-ref-by-id user-id])
        active (rf/subscribe [:active-medication-statements @patient-ref])
        other (rf/subscribe [:other-medication-statements @patient-ref])]
    (fn []
      ;;(print "t" (- (new js/Date) t1))
      (if (or @loading (< (- (new js/Date) t1) 300))
        [ui/activity-indicator]
        [ui/scroll-view
         (if-not (empty? @active)
           [section {:title "Active"}
            (for [[idx med-st] (map-indexed vector @active)]
              ^{:key idx}
              [section-row {:title (medication med-st)
                            :description (get-in med-st [:note 0 :text])
                            :on-press #()}])])

         (if-not (empty? @other)
           [section {:title "Other"}
            (for [[idx med-st] (map-indexed vector @other)]
              ^{:key idx}
              [section-row {:title (medication med-st)
                            :description (get-in med-st [:note 0 :text])
                            :on-press #()}])])

         (if (every? empty? [@active @other])
           [section-row {:title ""
                         :description "No data"
                         :on-press #()}])
         ])
      )))
