package assignment2.ProcessHandler;

import java.security.interfaces.ECPublicKey;

public class Burst implements Comparable {
    public int deviceId = 0; //the device the burst executes on
    private int duration; //how long the burst is
    private BurstType burstType; //burst type enum
    private int processId; //the id of the process it is associated with
    private Process processAssociated; //the process that owns this burst
    private int burstID; //burst id

    private int startTime = 0; //calculated value, default 0
    private int endTime; //calculated value


    public Burst(int deviceId, int time, Process processAssociated, BurstType burstType){
        this.duration = time;
        this.burstType = burstType;
        this.processId = processAssociated.getProcessId();
        this.processAssociated = processAssociated;

        //for CPU burstType the deviceId is always 0
        if (burstType == BurstType.CPU){
            this.deviceId = 0;
        }else{
            this.deviceId = deviceId;
        }
    }

    public Burst(int deviceId, int time, Process processAssociated, BurstType burstType, int id){
        this.duration = time;
        this.burstType = burstType;
        this.processId = processAssociated.getProcessId();
        this.processAssociated = processAssociated;
        this.burstID = id;

        //for CPU burstType the deviceId is always 0
            if (burstType == BurstType.CPU){
            this.deviceId = 0;
        }else{
            this.deviceId = deviceId;
        }
}

    @Override
    public int compareTo(Object compare) {
        int compareTime = ((Burst) compare).getTime();
        /* For Ascending order*/
        return this.getTime() - compareTime;
    }

    public Process getProcessAssociated(){
        return this.processAssociated;
    }


    public int getDeviceId(){
        return this.deviceId;
    }

    public BurstType getBurstType(){
        return this.burstType;
    }

    public int getTime(){
        return this.duration;
    }

    public void setStartTime(int time){
        this.startTime = time;
    }

    public void setEndTime(int time){
        this.endTime = time;
    }

    public int getStartTime(){
        return this.startTime;
    }

    public int getEndTime(){
        return this.endTime;
    }

    public void removeTime(int time){
        this.duration -= time;
    }

    public static Burst deepClone(Burst b){
        return new Burst(b.getDeviceId(), b.getTime(), b.getProcessAssociated(), b.getBurstType());
    }

    //used when deepClone is called but still need to link Bursts together
    public boolean equals(Burst b){
        if (b.processId == this.processId && b.getTime() == this.getTime() && b.getProcessAssociated() == this.processAssociated && this.burstType == b.burstType)
            return true;
        return false;
    }


}
