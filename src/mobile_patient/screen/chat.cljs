(ns mobile-patient.screen.chat
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [reagent.core :as r]
            [clojure.string :as str]
            [mobile-patient.color :as color]
            [mobile-patient.model.user :as user-model]
            [goog.i18n.DateTimeFormat]))

(def ds (ui/ReactNative.ListView.DataSource.
         #js{:rowHasChanged (fn[a b] (not= a b))
             :sectionHeaderHasChanged (fn[a b] (not= a b))
             }))


(def SectionHeader
  (r/reactify-component
   (fn [{:keys [section-id section-data]}]
     [ui/text {:style {:color color/pink
                       :font-weight :bold
                       :text-align :center
                       :padding 10
                       }}
      section-id])))

(def chat-message-row-comp
  (r/reactify-component
   (fn [props]
     (let [message (js->clj (:row props) :keywordize-keys true)
           user-id (get-in message [:author :id])
           user @(subscribe [:user-by-id user-id])
           this-user-id (:id @(subscribe [:domain-user]))
           user-msg? (= user-id this-user-id)
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
         (when-not user-msg? [ui/text {:style {:color "deepskyblue"}}
                              (user-model/get-official-name user)])
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

(defn build-ds-source [rows]
  (let [df (new goog.i18n.DateTimeFormat goog.i18n.DateTimeFormat.Format.MEDIUM_DATE)]
    (group-by #(->> (:sendtime %)
                    (js/Date.)
                    (.format df))
                    rows)))

(defn ChatScreen [_]
  (let [timer (atom nil)
        chat (subscribe [:chat])
        ;;this (r/current-component)
        input (atom nil)
        lv (atom nil)
        messages (subscribe [:messages])]
    (r/create-class
     {:display-name "ChatScreen"
      :component-did-mount (fn [] (reset! timer (js/setInterval #(dispatch [:get-messages (:id @chat)]) 1000)))
      :component-will-unmount #(js/clearInterval @timer)
      :reagent-render
      (fn [_]
        (let [source (build-ds-source @messages)]
          [ui/view {:style {:flex 1
                            :background-color "#f4f4f4"}}
           [ui/list-view {:style {:flex 1}
                          :enableEmptySections true
                          :on-content-size-change #(some-> @lv .scrollToEnd)
                          :dataSource (.cloneWithRowsAndSections ds
                                                                 (clj->js source))
                          :ref #(reset! lv %)
                          :render-section-header
                          (fn [section-data section-id]
                            (r/create-element SectionHeader #js{:section-data section-data
                                                                :section-id section-id}))

                          :render-row (fn [row section-id row-id hl-row]
                                        (r/create-element
                                         chat-message-row-comp
                                         #js{:row row
                                             :section-id section-id}))}]
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
            [send-msg-button input]]]))})))
