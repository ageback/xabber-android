package com.xabber.android.data.database;


import android.database.Cursor;
import android.os.Looper;

import com.xabber.android.data.Application;
import com.xabber.android.data.database.messagerealm.MessageItem;
import com.xabber.android.data.database.messagerealm.SyncInfo;
import com.xabber.android.data.database.sqlite.MessageTable;
import com.xabber.android.data.entity.AccountJid;
import com.xabber.android.data.entity.UserJid;
import com.xabber.android.data.log.LogManager;

import org.jxmpp.stringprep.XmppStringprepException;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;
import io.realm.annotations.RealmModule;

public class MessageDatabaseManager {
    private static final String REALM_MESSAGE_DATABASE_NAME = "xabber.realm";
    static final int REALM_MESSAGE_DATABASE_VERSION = 13;
    private final RealmConfiguration realmConfiguration;

    private static MessageDatabaseManager instance;

    private Realm realmUiThread;

    public static MessageDatabaseManager getInstance() {
        if (instance == null) {
            instance = new MessageDatabaseManager();
        }

        return instance;
    }

    private MessageDatabaseManager() {
        Realm.init(Application.getInstance());
        realmConfiguration = createRealmConfiguration();

        boolean success = Realm.compactRealm(realmConfiguration);
        System.out.println("Realm message compact database file result: " + success);

    }

    /**
     * Creates new realm instance for use from background thread.
     * Realm should be closed after use.
     *
     * @return new realm instance
     * @throws IllegalStateException if called from UI (main) thread
     */
    public Realm getNewBackgroundRealm() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Request background thread message realm from UI thread");
        }

        return Realm.getInstance(realmConfiguration);
    }

    /**
     * Returns realm instance for use from UI (main) thread.
     * Do not close realm after use!
     *
     * @return realm instance for UI thread
     * @throws IllegalStateException if called from background thread
     */
    public Realm getRealmUiThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Request UI thread message realm from non UI thread");
        }

        if (realmUiThread == null) {
            realmUiThread = Realm.getInstance(realmConfiguration);
        }

        return realmUiThread;
    }

    public static RealmResults<MessageItem> getChatMessages(Realm realm, AccountJid accountJid, UserJid userJid) {
        return getChatMessagesQuery(realm, accountJid, userJid)
                .findAllSorted(MessageItem.Fields.TIMESTAMP, Sort.ASCENDING);
    }

    public RealmResults<MessageItem> getChatMessagesAsync(AccountJid accountJid, UserJid userJid) {
        return getChatMessagesQuery(getRealmUiThread(), accountJid, userJid)
                .findAllSortedAsync(MessageItem.Fields.TIMESTAMP, Sort.ASCENDING);
    }

    public static RealmQuery<MessageItem> getChatMessagesQuery(Realm realm, AccountJid accountJid, UserJid userJid) {
        return realm.where(MessageItem.class)
                .equalTo(MessageItem.Fields.ACCOUNT, accountJid.toString())
                .equalTo(MessageItem.Fields.USER, userJid.toString())
                .isNotNull(MessageItem.Fields.TEXT)
                .isNotEmpty(MessageItem.Fields.TEXT);
    }


    void deleteRealm() {
        Realm realm = getNewBackgroundRealm();
        Realm.deleteRealm(realm.getConfiguration());
        realm.close();
    }

    void removeAccount(final String account) {
        Realm realm = getNewBackgroundRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(MessageItem.class)
                        .equalTo(MessageItem.Fields.ACCOUNT, account)
                        .findAll()
                        .deleteAllFromRealm();
            }
        });
        realm.close();
    }


    @RealmModule(classes = {MessageItem.class, SyncInfo.class})
    static class MessageRealmDatabaseModule {
    }

    private RealmConfiguration createRealmConfiguration() {
        return new RealmConfiguration.Builder()
                .name(REALM_MESSAGE_DATABASE_NAME)
                .schemaVersion(REALM_MESSAGE_DATABASE_VERSION)
                .modules(new MessageRealmDatabaseModule())
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm1, long oldVersion, long newVersion) {
                        RealmSchema schema = realm1.getSchema();

                        if (oldVersion == 1) {
                            schema.create(SyncInfo.class.getSimpleName())
                                    .addField(SyncInfo.FIELD_ACCOUNT, String.class, FieldAttribute.INDEXED)
                                    .addField(SyncInfo.FIELD_USER, String.class, FieldAttribute.INDEXED)
                                    .addField(SyncInfo.FIELD_FIRST_MAM_MESSAGE_MAM_ID, String.class)
                                    .addField(SyncInfo.FIELD_FIRST_MAM_MESSAGE_STANZA_ID, String.class)
                                    .addField(SyncInfo.FIELD_LAST_MESSAGE_MAM_ID, String.class)
                                    .addField(SyncInfo.FIELD_REMOTE_HISTORY_COMPLETELY_LOADED, boolean.class);

                            oldVersion++;
                        }

                        if (oldVersion == 2) {
                            schema.create(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.UNIQUE_ID, String.class, FieldAttribute.PRIMARY_KEY)
                                    .addField(MessageItem.Fields.ACCOUNT, String.class, FieldAttribute.INDEXED)
                                    .addField(MessageItem.Fields.USER, String.class, FieldAttribute.INDEXED)
                                    .addField(MessageItem.Fields.RESOURCE, String.class)
                                    .addField(MessageItem.Fields.ACTION, String.class)
                                    .addField(MessageItem.Fields.TEXT, String.class)
                                    .addField(MessageItem.Fields.TIMESTAMP, Long.class, FieldAttribute.INDEXED)
                                    .addField(MessageItem.Fields.DELAY_TIMESTAMP, Long.class)
                                    .addField(MessageItem.Fields.STANZA_ID, String.class)
                                    .addField(MessageItem.Fields.INCOMING, boolean.class)
                                    .addField(MessageItem.Fields.UNENCRYPTED, boolean.class)
                                    .addField(MessageItem.Fields.SENT, boolean.class)
                                    .addField(MessageItem.Fields.READ, boolean.class)
                                    .addField(MessageItem.Fields.DELIVERED, boolean.class)
                                    .addField(MessageItem.Fields.OFFLINE, boolean.class)
                                    .addField(MessageItem.Fields.ERROR, boolean.class)
                                    .addField(MessageItem.Fields.IS_RECEIVED_FROM_MAM, boolean.class)
                                    .addField(MessageItem.Fields.FILE_PATH, String.class)
                                    .addField(MessageItem.Fields.FILE_SIZE, Long.class);

                            oldVersion++;
                        }

                        if (oldVersion == 3) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addIndex(MessageItem.Fields.SENT);
                            oldVersion++;
                        }

                        if (oldVersion == 4) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.FORWARDED, boolean.class);
                            oldVersion++;
                        }

                        if (oldVersion == 5) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.ACKNOWLEDGED, boolean.class);
                            oldVersion++;
                        }

                        if (oldVersion == 6) {
                            schema.create("LogMessage")
                                    .addField("level", int.class)
                                    .addField("tag", Date.class)
                                    .addField("message", String.class)
                                    .addField("datetime", String.class);

                            oldVersion++;
                        }

                        if (oldVersion == 7) {
                            schema.remove("LogMessage");
                            oldVersion++;
                        }

                        if (oldVersion == 8) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.FILE_URL, String.class);
                            oldVersion++;
                        }

                        if (oldVersion == 9) {
                            schema.remove("BlockedContactsForAccount");
                            schema.remove("BlockedContact");
                            oldVersion++;
                        }

                        if (oldVersion == 10) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.IS_IN_PROGRESS, boolean.class);
                            oldVersion++;
                        }

                        if (oldVersion == 11) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.IS_IMAGE, boolean.class);
                            oldVersion++;
                        }

                        if (oldVersion == 12) {
                            schema.get(MessageItem.class.getSimpleName())
                                    .addField(MessageItem.Fields.IMAGE_WIDTH, Integer.class)
                                    .addField(MessageItem.Fields.IMAGE_HEIGHT, Integer.class);
                            oldVersion++;
                        }

                    }
                })
                .build();
    }

    void copyDataFromSqliteToRealm() {
        Realm realm = getNewBackgroundRealm();

        realm.beginTransaction();

        LogManager.i("DatabaseManager", "copying from sqlite to Reaml");
        long counter = 0;
        Cursor cursor = MessageTable.getInstance().getAllMessages();
        while (cursor.moveToNext()) {
            try {
                MessageItem messageItem = MessageTable.createMessageItem(cursor);
                realm.copyToRealm(messageItem);
            } catch (XmppStringprepException | UserJid.UserJidCreateException e) {
                LogManager.exception(this, e);
            }

            counter++;
        }
        cursor.close();
        LogManager.i("DatabaseManager", counter + " messages copied to Realm");

        LogManager.i("DatabaseManager", "onSuccess. removing messages from sqlite:");
        int removedMessages = MessageTable.getInstance().removeAllMessages();
        LogManager.i("DatabaseManager", removedMessages + " messages removed from sqlite");

        realm.commitTransaction();
        realm.close();
    }
}
