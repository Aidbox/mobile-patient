(ns mobile-patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [reg-event-db after reg-event-fx reg-fx
                          dispatch dispatch-sync subscribe reg-sub-raw]]
   [mobile-patient.color :as color]
   [re-frame.loggers :as rf.log]
   [mobile-patient.ui :as ui]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [mobile-patient.db :as db :refer [app-db]]))

(def warn (js/console.warn.bind js/console))
(rf.log/set-loggers!
 {:warn (fn [& args]
          (println (first args))
          (cond
            (= "re-frame: overwriting" (first args)) nil
            :else (apply warn args)))})

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Views----------------------------------------------------------

(def ds (ui/ReactNative.ListView.DataSource. #js{:rowHasChanged (fn[a b] false)}))
(def font-size 18)

(def chat-row-comp
  (r/reactify-component
   (fn [props]
     (let [row (props :row)
           navigation (props :navigation)
           chat @row]
       [ui/touchable-highlight {:on-press #(do
                                             (dispatch [:set-chat chat])
                                             (navigation.navigate "Chat" #js{:chat-name (:name chat)}))
                                :underlay-color "white"}
        [ui/view {:style {:flex 1
                          :flex-direction :row
                          :margin-top 10
                          :margin-bottom 10
                          :margin-left 20}}
         [ui/view {:style {:flex 0.9}}
          [ui/text {:style {:color "black"
                            :font-weight :bold
                            :font-size font-size}}
           (:name chat)]
          [ui/text {:style {:color "#919291"
                            :font-size font-size}}
           (str/join ", " (map :id (:participants chat)))]]
          [ui/icon {:style {:flex 0.1
                            :align-self :center}
                    :name "chevron-right"
                    :size 36
                    :color color/pink}]]]))))

(defn dispatch-get-chats-event []
  (dispatch [:get-chats]))

(defn dispatch-get-messages-event []
  (let [chat (subscribe [:chat])]
    (when chat
      (dispatch [:get-messages (:id @chat)]))))

(defonce do-get-chats (js/setInterval dispatch-get-chats-event 3000))
(defonce do-get-messages (js/setInterval dispatch-get-messages-event 1000))

(def chat-row-sep
  (r/create-element
   (r/reactify-component
    (fn [_] [ui/view {:style {:border-width 1
                              :border-color "#d7d7d7"}}]))
   #js{}))

(defn ChatsScreen [{:keys [navigation]}]
  (fn [_]
    (let [chats (subscribe [:chats])
          source (map #(r/atom %) @chats)]
      [ui/view {:style {:background-color "white"
                        :flex 1}}
       [ui/text {:style {:margin-left 20
                         :margin-top 20
                         :margin-bottom 20
                         :color color/pink
                         :font-size font-size}}
        "Practice Groups"]
       [ui/list-view {:dataSource (.cloneWithRows ds (clj->js source))
                      :enableEmptySections true
                      :render-separator (fn [_] chat-row-sep)
                      :render-row (fn [row]
                                    (r/create-element
                                     chat-row-comp
                                     #js{:row row
                                         :navigation navigation}))}]])))

(def chat-message-row-comp
  (r/reactify-component
   (fn [props]
     (let [message @(props :row)
           author (get-in message [:author :id])
           user (subscribe [:user])
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
        input (atom nil)]
    (fn [_]
      (let [messages (subscribe [:messages])
            source (map #(r/atom %) @messages)]
        [ui/view {:style {:flex 1
                          :background-color "#f4f4f4"}}
         [ui/list-view {:style {:flex 1}
                        :enableEmptySections true                        
                        :dataSource (.cloneWithRows ds (clj->js source))
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

;; -- Subscriptions----------------------------------------------------------
(reg-sub-raw
 :user
 (fn [db _] (reaction (:user @db))))

(reg-sub-raw
 :chats
 (fn [db _] (reaction (:chats @db))))

(reg-sub-raw
 :chat
 (fn [db _] (reaction (:chat @db))))

(reg-sub-raw
 :messages
 (fn [db _] (reaction (:messages @db))))

(reg-sub-raw
 :message
 (fn [db _] (reaction (:message @db))))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(reg-event-db
 :on-chats
 (fn [db [_ value]]
   (let [chats (map :resource (:entry value))]
     (if (not (= (:chats db) chats))
       (assoc db :chats chats)
       db))))

(reg-event-db
 :set-chat
 (fn [db [_ chat]]
   (-> db
       (assoc :chat chat)
       (assoc :messages []))))

(reg-event-db
 :set-message
 (fn [db [_ value]]
   (assoc db :message value)))

(reg-event-db
 :on-messages
 (fn [db [_ value]]
   db
   (let [messages (map :resource (:entry value))]
     (if (not (= (:messages db) messages))
       (assoc db :messages messages)
       db))))

(reg-event-fx
 :on-send-message
 (fn [db [_ value]]
   (println value)
   {}))

(defn parms->query [parms]
  (if parms
    (str "?"
         (clojure.string/join
          "&"
          (map #(str
                 (js/encodeURIComponent (name (first %)))
                 "="
                 (js/encodeURIComponent (second %)))
               parms)))
    nil))

(reg-fx
 :fetch
 (fn [{:keys [:uri :opts :success]}]
   (let [base-url (subscribe [:get-in [:config :base-url]])]
     (-> (js/fetch (str @base-url uri (parms->query (:parms opts)))
                   (clj->js (merge {:method "GET"
                                    :headers {"Content-Type" "application/json"}}
                                   opts)))
         (.then #(.json %))
         (.then
          (fn [response]
            (dispatch [success (js->clj response :keywordize-keys true)])))
         (.catch #(println "Fetch error" %)))
     {})))

(reg-event-fx
 :get-chats
 (fn [_]
   (let [user (subscribe [:get-in [:user]])]
     {:fetch {:uri "/Chat"
              :success :on-chats
              :opts {:parms {:participant @user}}}})))

(reg-event-fx
 :get-messages
 (fn [_ [_ chat-id]]
   (let [user (subscribe [:get-in [:user]])]
     {:fetch {:uri "/Message"
              :success :on-messages
              :opts {:parms {:chat chat-id}}}})))

(reg-event-fx
 :send-message
 (fn [_]
   (let [message @(subscribe [:get-in [:message]])
         user @(subscribe [:get-in [:user]])
         chat @(subscribe [:get-in [:chat]])
         msg {:resourceType "Message"
              :body message
              :chat {:id (:id chat)
                     :resourceType "Chat"}
              :author {:id user
                       :resourceType "User"}}]
     (if (and message (not (clojure.string/blank? message)))
       {:fetch {:uri "/Message"
                :success :on-send-message
                :opts {:method "POST"
                       :headers {"content-type" "application/json"}
                       :body (.stringify js/JSON (clj->js msg))}}
        :dispatch [:set-message ""]}
       {}))))

