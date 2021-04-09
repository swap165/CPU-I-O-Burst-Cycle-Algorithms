package assignment2.FCFS;

import assignment2.DataStructures.DoublyLinkedList;
import assignment2.ProcessHandler.Burst;
import assignment2.ProcessHandler.BurstType;
import assignment2.ProcessHandler.Process;
import assignment2.ProcessHandler.State;

import java.util.ArrayList;
import java.util.HashMap;

//Composition structure. Why use a Manager class? Because of SOLID Principles. S stands for single responsibility, so I wanted to ensure that the cpuScheduler and ioScheduler have a single responsibility
public class FCFSSchedulerManager {
    private final DoublyLinkedList cpuScheduler; //only 1 FCFSScheduler device. Doubly LinkedList Algorithm
    private final HashMap<Integer, IOScheduler> ioSchedulerHashMap; //potentially multiple IOScheduler devices so we need to keep track of them all - Runtime O(1)

    public FCFSSchedulerManager(){
        cpuScheduler= new DoublyLinkedList();
        ioSchedulerHashMap = new HashMap<>();
    }

    public void ganttChart() {
        cpuScheduler.printAllBursts("----CPUScheduler Device 0 -----");
        for (int i : ioSchedulerHashMap.keySet()) {
            ioSchedulerHashMap.get(i).printAllBursts("---IOScheduler Device " + i + "---");
        }

    }

    //Picks the first process that arrives, executes all the bursts for that process and moves onto the next burst that has shortest arrivalTime
    public void addProcesses(ArrayList<Process> processes) {
        int timeExecutedSoFar = 0;

        while (processes.size() > 0){
            Process process = processes.get(0);
            for (int i = 1; i < processes.size(); i++){
                if (processes.get(i).getArrivalTime() < process.getArrivalTime()){
                    process = processes.get(i);
                }else if(processes.get(i).getArrivalTime() == process.getArrivalTime()) {
                    //If the arrival time of the current process is 0 and firstArrived we have stored is 0, then choose the one with the highest priority to be considered firstArrived
                    process = process.getPriority() < processes.get(i).getPriority() ? process : processes.get(i);
                }
            }


        process.setState(State.READY); //adding state to cpuScheduler so state becomes ready

        //First process
        if (cpuScheduler.isEmpty()) {
            //Adding bursts
            for (int i = 0; i < process.getBursts().size(); i++) {
                Burst currBurst = process.getBursts().get(i); //current burst we are looking at
                Burst prevBurst = null;

                //Below calculates burst order and burst time for a specific process
                if (i > 0) //only get prev burst if i > 0 to avoid indexOutOfBounds
                    prevBurst = process.getBursts().get(i - 1);

                //first burst added to scheduler
                if (i == 0)
                    currBurst.setStartTime(0);
                else
                    currBurst.setStartTime(prevBurst.getEndTime()); //start time is just when the last burst ended
                currBurst.setEndTime(currBurst.getTime() + currBurst.getStartTime()); //calculating endtime

                //if CPU add to cpuScheduler
                timeExecutedSoFar+=currBurst.getTime();
                if (currBurst.getBurstType() == BurstType.CPU) {
                    cpuScheduler.addLast(currBurst); //LinkedList type datastructure which will hold the bursts
                } else if (currBurst.getBurstType() == BurstType.I_O) {
                    IOScheduler ioScheduler = ioSchedulerHashMap.getOrDefault(currBurst.getDeviceId(), new IOScheduler()); //get the IOScheduler if it exists or make a new one
                    ioScheduler.addLast(currBurst);
                    ioScheduler.setId(currBurst.getDeviceId()); //just incase we made a new IOScheduler from this instance
                    ioSchedulerHashMap.put(currBurst.getDeviceId(), ioScheduler); //put into appropriate hashmap
                }
            }

        }else{
            //First we have to get the previous burst which could be in any device
            Burst prevBurst = cpuScheduler.getLast().getBurst();

            for (IOScheduler s : ioSchedulerHashMap.values()) {
                if (s.getLast().getBurst().getEndTime() > prevBurst.getEndTime()) {
                    prevBurst = s.getLast().getBurst();
                }
            }

            for (int i = 0; i < process.getBursts().size(); i++) {
                Burst currBurst = process.getBursts().get(i); //current burst we are looking at
                currBurst.setStartTime(prevBurst.getEndTime());
                currBurst.setEndTime(prevBurst.getEndTime() + currBurst.getTime());
                prevBurst = currBurst;

                if (i == 0){ //handling just in case next process arrives even later after the last process finishes
                    if (timeExecutedSoFar < process.getArrivalTime()){
                        currBurst.setStartTime(process.getArrivalTime());
                        currBurst.setEndTime(prevBurst.getEndTime() + currBurst.getTime());
                    }
                }


                timeExecutedSoFar+=currBurst.getTime();
                if (currBurst.getBurstType() == BurstType.CPU) {
                    cpuScheduler.addLast(currBurst); //LinkedList type datastructure which will hold the bursts
                } else if (currBurst.getBurstType() == BurstType.I_O) {
                    IOScheduler ioScheduler = ioSchedulerHashMap.getOrDefault(currBurst.getDeviceId(), new IOScheduler()); //get the IOScheduler if it exists or make a new one
                    ioScheduler.addLast(currBurst);
                    ioScheduler.setId(currBurst.getDeviceId()); //just incase we made a new IOScheduler from this instance
                    ioSchedulerHashMap.put(currBurst.getDeviceId(), ioScheduler); //put into appropriate hashmap
                    }
                }

            }
            processes.remove(process);
        }


    }

    //abstracted in FCFSScheduler
    //work on this
    public void getStateAtTime(int time){
        System.out.println(cpuScheduler.getStateAtTime(time) + " at time " + time);
    }

    //Calculates averageTurnAroundTime by calculating the endtime - arrivalTime for each process then finds the average
    public void averageTurnAroundTime(){
        HashMap<Process, Integer> endTimes = new HashMap<>();
        DoublyLinkedList.Node currNode = cpuScheduler.getLast();

        while (currNode != null && currNode.getBurst() != null){
            if (endTimes.get(currNode.getBurst().getProcessAssociated()) == null && currNode.getBurst() != null){
                endTimes.put(currNode.getBurst().getProcessAssociated(), currNode.getBurst().getEndTime());
            }
            currNode = currNode.getPrev();
        }

        HashMap<Process, Integer> turnAroundTimeForProcess = new HashMap<>();
        for (Process p : endTimes.keySet()){
            turnAroundTimeForProcess.put(p, endTimes.get(p) - p.getArrivalTime());
        }

        double sum = 0;
        double numOfKeys = 0;
        System.out.println("The Individual Turnaround Time for Each Process is... ");
        for (Process p : turnAroundTimeForProcess.keySet()){
            numOfKeys++;
            int turnAroundTime = turnAroundTimeForProcess.get(p);
            sum+=turnAroundTime;
            System.out.println("-Process Id: " + p.getProcessId() + " Has an individual turnaround time of " + turnAroundTime);
        }

        System.out.println("----------------------------------");
        System.out.println("The Average Turnaround Time for these process' for NonPreemptive FCFS Algorithm is: " + sum / numOfKeys);
        System.out.println("----------------------------------");

    }
    //Calculates average waitingTime by calculating processStartTime - arrivalTime for each process and returns the average
    public void averageWaitingTime(){
        HashMap<Process, Integer> startTimes = new HashMap<>();


        DoublyLinkedList.Node currNode = cpuScheduler.getFirst();

        while (currNode != null && currNode.getBurst() != null){
            if (startTimes.get(currNode.getBurst().getProcessAssociated()) == null && currNode.getBurst() != null){
                startTimes.put(currNode.getBurst().getProcessAssociated(), currNode.getBurst().getStartTime());
            }
            currNode = currNode.getNext();
        }



        HashMap<Process, Integer> waitingTimeForProcess = new HashMap<>();
        for (Process p : startTimes.keySet()){
            waitingTimeForProcess.put(p, startTimes.get(p) - p.getArrivalTime());
        }

        double sum = 0;
        double numOfKeys = 0;
        System.out.println("The Individual Waiting Time for Each Process is... ");
        for (Process p : waitingTimeForProcess.keySet()){
            numOfKeys++;
            int waitingTime = waitingTimeForProcess.get(p);
            sum+=waitingTime;
            System.out.println("-Process Id: " + p.getProcessId() + " Has an individual waiting time of " + waitingTime);
        }

        System.out.println("----------------------------------");
        System.out.println("The Average Waiting Time for these process' for NonPreemptive FCFS Algorithm is: " + sum / numOfKeys);
        System.out.println("----------------------------------");

    }


}
