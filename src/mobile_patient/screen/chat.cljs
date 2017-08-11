(ns mobile-patient.screen.chat
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [reagent.core :as r]
            [clojure.string :as str]
            [mobile-patient.color :as color]))

(def chat-message-row-comp
  (r/reactify-component
   (fn [props]
     (let [message @(props :row)
           author (get-in message [:author :id])
           user (subscribe [:user-id])
           user-msg? (= author @user)
           color (if user-msg? "#ffffff" "#e1e1e1")
           spacer [ui/view {:style {:flex 0.2}}]]
       [ui/view {:style {:margin-top 10
                         :margin-bottom 5
                         :margin-left 30
                         :margin-right 30
                         :flex 1
                         :flex-direction :row}}
        (when user-msg? spacer)
        [ui/view {:style  {:flex 0.8
                           :padding-top 20
                           :padding-bottom 20
                           :padding-left 20
                           :padding-right 15
                           :background-color color
                           :border-radius 60}}
         (when-not user-msg? [ui/text {:style {:color "deepskyblue"}} author])
         [ui/text (:body message)]]
        (when-not user-msg? spacer)]))))

(defn send-msg-button [input]
  (let [this (r/current-component)]
    [ui/touchable-highlight {:style {:margin-left 5
                                     :margin-right 5}
                             :underlay-color "#cccccc"
                             :on-press #(do
                                          (.setNativeProps @input #js{:text ""})
                                          (.dismiss (.-Keyboard ui/ReactNative))
                                          (dispatch [:send-message]))}
     [ui/icon {:name "send"
               :size 36
               :color "deepskyblue"
               :margin-right 8}]]))

(defn ChatScreen [_]
  (let [this (r/current-component)
        input (atom nil)
        lv (atom nil)]
    (fn [_]
      (let [messages (subscribe [:messages])
            source (map #(r/atom %) @messages)]
        [ui/view {:style {:flex 1
                          :background-color "#f4f4f4"}}
         [ui/list-view {:style {:flex 1}
                        :enableEmptySections true
                        :on-content-size-change #(some-> @lv .scrollToEnd)
                        :dataSource (.cloneWithRows ds (clj->js source))
                        :ref #(reset! lv %)
                        :render-row (fn [row]
                                      (r/create-element
                                       chat-message-row-comp
                                       #js{:row row}))}]
         [ui/view {:style {:background-color "#ffffff"
                           :flex-direction :row
                           :padding-top 20
                           :padding-bottom 20
                           :align-items :center}}
          [ui/input {:style {:background-color "#f4f4f4"
                             :padding-left 15
                             :margin-left 30
                             :border-width 1
                             :border-color "#cccccc"
                             :border-radius 60
                             :flex 1}
                     :underline-color-android "transparent"
                     :multiline true
                     :ref #(reset! input %)
                     :on-change-text #(dispatch [:set-message %])}]
          [send-msg-button input]]]))))
