package leap.droidcord;

import java.io.OutputStream;
import java.util.Date;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONObject;

public class HeartbeatThread extends Thread {
    leap.droidcord.State s;
    int lastReceived;
    volatile boolean stop;
    private OutputStream os;
    private long lastHeartbeat;
    private int interval;

    public HeartbeatThread(leap.droidcord.State s2, OutputStream os,
                           int interval) {
        this.s = s2;
        this.os = os;
        this.interval = interval;
        this.lastReceived = -1;
    }

    @SuppressWarnings("deprecation")
    public void run() {
        try {
            while (true) {
                if (stop)
                    break;
                long now = new Date().getTime();

                if (interval > 0 && now > lastHeartbeat + interval) {
                    JSONObject hbMsg = new JSONObject();
                    hbMsg.put("op", 1);
                    if (lastReceived >= 0) {
                        hbMsg.put("d", lastReceived);
                    } else {
                        hbMsg.put("d", JSON.json_null);
                    }

                    os.write((hbMsg.build() + "\n").getBytes());
                    os.flush();
                    lastHeartbeat = now;
                }

                Thread.sleep(interval);
            }
        } catch (Exception e) {
            s.gateway.stopMessage = "Heartbeat thread error: " + e.toString();
            s.gateway.stop = true;
        }
    }
}