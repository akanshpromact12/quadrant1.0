package com.gametime.quadrant.InvitePeopleModule;

/**
 * Created by Akansh on 29-12-2017.
 */

class InvitePeopleContract {
    public interface InvitePeopleView {
        void showMessage(String msg);
    }

    public interface InvitePeopleActions {
        void invitePeople();
    }
}
