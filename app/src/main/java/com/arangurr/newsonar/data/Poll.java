package com.arangurr.newsonar.data;

import java.util.UUID;

/**
 * Created by Rodrigo on 20/03/2017.
 */

public class Poll extends MessagePayload {

    private boolean mPasswordProtected = false;
    private String mPassword;
    private UUID mId;
    private UUID mOwnerId;

    public boolean isPasswordProtected() {
        return mPasswordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        mPasswordProtected = passwordProtected;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public UUID getUuid() {
        return mId;
    }

    public void setUuid(UUID id) {
        mId = id;
    }

    public UUID getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(UUID ownerId) {
        mOwnerId = ownerId;
    }
}
