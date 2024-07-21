package leap.droidcord;

public class StopTypingThread extends Thread {
    leap.droidcord.State s;
    Long userID;

    public StopTypingThread(leap.droidcord.State s, long userID) {
        this.s = s;
        this.userID = userID;
    }

    public void run() {
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

        for (int i = 0; i < s.typingUsers.size(); i++) {
            if (s.typingUserIDs.elementAt(i) == userID) {
                s.typingUsers.removeElementAt(i);
                s.typingUserIDs.removeElementAt(i);
                
                /*if (s.oldUI) {
                    s.oldChannelView.update();
                } else {
                    s.channelView.repaint();
                }*/
                return;
            }
        }
    }
}