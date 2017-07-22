package com.xabber.android.data.xaccount;

import java.util.List;

/**
 * Created by valery.miller on 17.07.17.
 */

public class XabberAccountDTO {

    private int id;
    private String username;
    private String first_name;
    private String last_name;
    private String registration_date;
    private List<XMPPUserDTO> xmpp_users;

    public XabberAccountDTO(int id, String username, String first_name, String last_name, String registration_date, List<XMPPUserDTO> xmpp_users) {
        this.id = id;
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.registration_date = registration_date;
        this.xmpp_users = xmpp_users;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public String getRegistrationDate() {
        return registration_date;
    }

    public List<XMPPUserDTO> getXmppUsers() {
        return xmpp_users;
    }
}

