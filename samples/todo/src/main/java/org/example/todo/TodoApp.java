package org.example.todo;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import org.nexusdata.core.*;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.predicate.Predicate;
import org.nexusdata.predicate.parser.PredicateParser;
import org.nexusdata.store.AndroidSqlPersistentStore;
import java.io.IOException;

public class TodoApp extends Application {

    private static PersistentStoreCoordinator storeCoordinator;
    private static ObjectContext mainObjectContext;
    private static TodoApp app;

    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
    }

    public static PersistentStoreCoordinator getStoreCoordinator() {
        if (storeCoordinator == null) {
            ObjectModel model;
            try {
                model = new ObjectModel(app.getAssets().open("todo.model.json"));
            } catch (IOException ex) {
                throw new RuntimeException("Could not find models file", ex);
            }

            storeCoordinator = new PersistentStoreCoordinator(model);

            Context ctx = app.getApplicationContext();
            PersistentStore cacheStore = new AndroidSqlPersistentStore(ctx, ctx.getDatabasePath("todo"));
            storeCoordinator.addStore(cacheStore);
        }

        return storeCoordinator;
    }

    public static ObjectContext getMainObjectContext() {
        if (mainObjectContext == null) {
            mainObjectContext = new ObjectContext(getStoreCoordinator());

            ObjectContextNotifier.registerListener(new ObjectContextNotifier.DefaultObjectContextListener() {
                @Override
                public void onPostSave(ObjectContext context, ChangedObjectsSet changedObjects) {
                    if (context != mainObjectContext && context.getPersistentStoreCoordinator() == mainObjectContext.getPersistentStoreCoordinator()) {
                        mainObjectContext.mergeChangesFromSaveNotification(changedObjects);
                    }
                }
            });
        }

        return mainObjectContext;
    }
}
