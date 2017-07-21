package com.xabber.android.data.database.realm;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by valery.miller on 19.07.17.
 */

public class XabberAccountRealm extends RealmObject {

    @PrimaryKey
    @Required
    private String id;

    private String username;
    private String firstName;
    private String lastName;
    private String registerDate;
    private RealmList<XMPPUserRealm> xmppUsers;

    public XabberAccountRealm(String id) {
        this.id = id;
    }

    public XabberAccountRealm() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public RealmList<XMPPUserRealm> getXmppUsers() {
        return xmppUsers;
    }

    public void setXmppUsers(RealmList<XMPPUserRealm> xmppUsers) {
        this.xmppUsers = xmppUsers;
    }
}

