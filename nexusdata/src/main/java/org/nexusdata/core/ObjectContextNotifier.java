package org.nexusdata.core;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a singleton class used to register for notifications from {@link ObjectContext} events, such as
 * inserted objects, changed objects, etc.
 */
public class ObjectContextNotifier {

    /**
     * Defines the interface which {@link ObjectContext} listeners must implement to receive notifications of relevant
     * events.
     */
    public static interface ObjectContextListener {
        /**
         * Called when any set of objects have changed since the last event loop. Changes includes objects that have
         * have been inserted, removed, updated, or refreshed.
         *
         * @param context           the context in which the changed objects belong to
         * @param changedObjects    a notification object containing the objects that have been inserted, removed,
         *                          updated, or refreshed
         */
        public void onObjectsChanged(ObjectContext context, ObjectsChangedNotification changedObjects);

        /**
         * Called when the context is about to be saved.
         *
         * @param context the context instance that will be saved
         */
        public void onPreSave(ObjectContext context);

        /**
         * Called when the context has just been saved.
         *
         * @param context           the context instance that has been saved
         * @param changedObjects    a reference to a collection of the objects that have been changed
         */
        public void onPostSave(ObjectContext context, ChangedObjectsSet changedObjects);
    }

    /**
     * Provides a default empty implementation of the {@link ObjectContextListener}. This is useful when you want
     * to only override specific methods and leave the rest empty.
     */
    public static abstract class DefaultObjectContextListener implements ObjectContextListener {
        @Override
        public void onPreSave(ObjectContext context) {
            // do nothing by default
        }

        @Override
        public void onObjectsChanged(ObjectContext context, ObjectsChangedNotification objectsChanged) {
            // do nothing by default
        }

        @Override
        public void onPostSave(ObjectContext context, ChangedObjectsSet changedObjects) {
            // do nothing by default
        }
    }

    private static Set<ObjectContextListener> allContextsListeners = new LinkedHashSet<ObjectContextListener>();
    private static Map<ObjectContext, Set<ObjectContextListener>> contextListeners = new HashMap<ObjectContext, Set<ObjectContextListener>>();

    static void notifyListenersOfPreSave(ObjectContext context) {
        for (ObjectContextListener listener : getListeners(context)) {
            listener.onPreSave(context);
        }
    }

    static void notifyListenersOfPostSave(ObjectContext context, ChangedObjectsSet changedObjects) {
        for (ObjectContextListener listener : getListeners(context)) {
            listener.onPostSave(context, changedObjects);
        }
    }

    static void notifyListenersOfObjectsChanged(ObjectContext context, ObjectsChangedNotification changedObjects) {
        for (ObjectContextListener listener : getListeners(context)) {
            listener.onObjectsChanged(context, changedObjects);
        }
    }

    static boolean hasListeners(ObjectContext context) {
        Set<ObjectContextListener> listeners = contextListeners.get(context);
        return !allContextsListeners.isEmpty() || (listeners != null && !listeners.isEmpty());
    }

    private static Set<ObjectContextListener> getListeners(ObjectContext context) {
        Set<ObjectContextListener> allListeners = getListenersForContext(context);

        if (!allContextsListeners.isEmpty()) {
            // make a copy so we don't modify original
            allListeners = new LinkedHashSet<ObjectContextListener>(allListeners);
            allListeners.addAll(allContextsListeners);
        }

        return allListeners;
    }

    private static Set<ObjectContextListener> getListenersForContext(ObjectContext context) {
        Set<ObjectContextListener> listeners = contextListeners.get(context);

        if (listeners == null) {
            listeners = new LinkedHashSet<ObjectContextListener>();
            contextListeners.put(context, listeners);
        }

        return listeners;
    }

    /**
     * Registers a listener to be notified of events from the specified ObjectContext. If the listener was previously
     * registered for events from all ObjectContexts, registration is ignored.
     * <p>
     * <b>Note:</b> An application must unregister the listener when it is no longer interested in the events to prevent
     *              memory leaks.
     *
     * @param forContext    the ObjectContext to listen for
     * @param listener      the listener to register
     * @see #registerListener(ObjectContextListener)
     */
    public static void registerListener(ObjectContext forContext, ObjectContextListener listener) {
        if (!allContextsListeners.contains(listener)) {
            Set<ObjectContextListener> listeners = getListenersForContext(forContext);
            listeners.add(listener);
        }
    }

    /**
     * Registers a listener to be notified of events from all ObjectContexts used by the application.
     * <p>
     * <b>Note:</b> An application must unregister the listener when you are no longer interested in the events to prevent
     *              memory leaks.
     *
     * @param listener      the listener to register
     * @see   #registerListener(ObjectContext, ObjectContextListener)
     */
    public static void registerListener(ObjectContextListener listener) {
        allContextsListeners.add(listener);
    }

    /**
     * Unregisters a listener from receiving events for the specified object context.
     *
     * @param forContext    the ObjectContext to unregister for
     * @param listener      the listener to unregister
     * @see #unregisterListener(ObjectContextListener)
     */
    public static void unregisterListener(ObjectContext forContext, ObjectContextListener listener) {
        Set<ObjectContextListener> listeners = getListenersForContext(forContext);
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            // remove context from collection to prevent memory leaks
            contextListeners.remove(forContext);
        }
    }

    /**
     * Unregisters a listener from receiving events for all ObjectContexts used by the application.
     *
     * @param listener      the listener to unregister
     * @see #unregisterListener(ObjectContext, ObjectContextListener)
     */
    public static void unregisterListener(ObjectContextListener listener) {
        allContextsListeners.remove(listener);
    }
}
