package assignment2.ProcessHandler;

import java.util.Comparator;
import java.util.LinkedList;

public class Process implements Comparable {
    private int processId;
    private int arrivalTime;
    private int priority;
    private State state;

    private LinkedList<Burst> bursts = new LinkedList(); //LinkedList so the bursts are in order in which they are added via addBurst

    public Process(int processId, int arrivalTime, int priority) {
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.state = State.NEW; //initial state is NEW, changes to READY when added to scheduler
    }


    public void addBurst(Burst b){
        bursts.add(b);
    }


    @Override
    public int compareTo(Object compare) {
        int compareTime = ((Process) compare).getArrivalTime();
        /* For Ascending order*/
        return this.getArrivalTime() - compareTime;
    }


    public void printBursts(){
        for (int i = 0; i < bursts.size(); i++){
            System.out.println(bursts.get(i).getTime());
            if (bursts.get(i).getTime() == 1){
                System.out.println("--Prev--");
                System.out.println(bursts.get(i-1).getTime());
            }
        }
    }

    public int getProcessId() {
        return processId;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getPriority() {
        return priority;
    }

    public State getState() {
        return state;
    }
    public void setState(State s){
        this.state = s;
    }

    public LinkedList<Burst> getBursts() {
        return bursts;
    }


}
