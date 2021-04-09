package assignment2.NonPPriority;

import assignment2.DataStructures.DoublyLinkedList;
import assignment2.FCFS.IOScheduler;
import assignment2.ProcessHandler.Burst;
import assignment2.ProcessHandler.BurstType;
import assignment2.ProcessHandler.Process;
import assignment2.ProcessHandler.State;

import java.util.ArrayList;
import java.util.HashMap;

public class NonPPrioritySchedulerManager {
    private DoublyLinkedList cpuScheduler;
    private HashMap<Integer, IOScheduler> ioSchedulerHashMap; //potentially multiple IOScheduler devices so we need to keep track of them all - Runtime O(1)

    public NonPPrioritySchedulerManager(){
        this.cpuScheduler = new DoublyLinkedList();
        this.ioSchedulerHashMap = new HashMap<>();
    }
    public void ganttChart() {
        cpuScheduler.printAllBursts("----CPUScheduler Device 0 -----");
        for (int i : ioSchedulerHashMap.keySet()) {
            ioSchedulerHashMap.get(i).printAllBursts("---IOScheduler Device " + i + "---");
        }

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
        System.out.println("The Average Turnaround Time for these process' for NonPreemptive Priority Algorithm is: " + sum / numOfKeys);
        System.out.println("----------------------------------");

    }

    //Helper function
    private void addBurstToScheduler(Burst currBurst) {
        if (currBurst.getBurstType() == BurstType.CPU) {
            cpuScheduler.addLast(currBurst); //LinkedList type datastructure which will hold the bursts
        } else if (currBurst.getBurstType() == BurstType.I_O) {
            IOScheduler ioScheduler = ioSchedulerHashMap.getOrDefault(currBurst.getDeviceId(), new IOScheduler()); //get the IOScheduler if it exists or make a new one
            ioScheduler.addLast(currBurst);
            ioScheduler.setId(currBurst.getDeviceId()); //just incase we made a new IOScheduler from this instance
            ioSchedulerHashMap.put(currBurst.getDeviceId(), ioScheduler); //put into appropriate hashmap
        }
    }
    //Picks the best process based on priority, executes all the bursts for that process and moves onto the next burst that has the next best priority relative to arrivalTime
    public void addProcesses(ArrayList<Process> processes){
        //Look at the proccess'
        //The first process to get executed is the first one to arrive
        //If two proccess' arrive at the same earliest time OR, after one proccess has FINISHED executing there waits another 4 process' to be executed
        //the process to be chosen out of those 4 is the one with the smallest priority value of the process

        processes.sort((Process p1, Process p2) -> p1.compareTo(p2)); //Sort by arrival time - nlogn procedure

        int timeExecutedSoFar = 0; //this has to be computed for each new process added


        while (processes.size() > 0) {

            if (timeExecutedSoFar == 0){
                Process process = processes.get(0);

                //If adding first process, we base it off of the first one to arrive. If two arrive at same time we base it off of shortest first burst. If two arrive at same time and have same first burst, we base it off of priority
                for (int i = 1; i < processes.size(); i++) {
                    if (processes.get(i).getArrivalTime() < process.getArrivalTime()) {
                        process = processes.get(i);
                    }else if (processes.get(i).getArrivalTime() == process.getArrivalTime()) {//if two Proccess' have share the same arrival time which is earlier than any other process proceed to these conditions

                        //if two processes which have the same arrival earliest arrival time we pick based off of priority, but if they also have same priority we pick by SJF
                        if (processes.get(i).getPriority() == process.getPriority()){
                            process = process.getBursts().getFirst().getTime() < processes.get(i).getBursts().getFirst().getTime() ? process : processes.get(i);
                        }else {
                            //else we pick off of priority
                            process = process.getPriority() < processes.get(i).getPriority() ? process : processes.get(i);
                        }
                    }
                }

                process.setState(State.READY);
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


                        timeExecutedSoFar+=currBurst.getTime();
                        addBurstToScheduler(currBurst);
                    }
                processes.remove(process);

            }else{
                //Else: Any process added after the first process. Here we have to handle diff cases because we have to select the next burst based off of shortest first burst based on what has arrived in the time the first process took to execute

                //selecting potential candidates for next process. Candidates are what have arrived during the time the previous process took to execute
                ArrayList<Process> candidatesForNext = new ArrayList();
                for (int i = 0; i < processes.size(); i++) {
                    if (processes.get(i).getArrivalTime() <= timeExecutedSoFar){
                        candidatesForNext.add(processes.get(i));
                    }
                }

                Process process = processes.get(0); //defaulting to 0, this will hold ONLY if candidatesForNext.size() == 0. In this case it'll just picked the next one to arrive. Which we know will be satisfied because we sorted the list by arrival time up top

                //Selecting the candidate that has shorted job first
                if (candidatesForNext.size() > 0) {
                    process = candidatesForNext.get(0);
                    for (int i = 1; i < candidatesForNext.size(); i++) {
                        if (candidatesForNext.get(i).getPriority() < process.getPriority()) {
                            process = candidatesForNext.get(i);
                        } else if (processes.get(i).getPriority() == process.getPriority()) {
                            process = process.getArrivalTime() < processes.get(i).getArrivalTime() ? process : processes.get(i);
                        }
                    }
                }

                process.setState(State.READY);


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

                        if (i == 0){
                            if (timeExecutedSoFar < process.getArrivalTime()){
                                currBurst.setStartTime(process.getArrivalTime());
                                currBurst.setEndTime(prevBurst.getEndTime() + currBurst.getTime());
                            }
                        }

                        //if CPU add to cpuScheduler
                        timeExecutedSoFar+=currBurst.getTime();
                        addBurstToScheduler(currBurst);
                    }
                processes.remove(process);
                }


            }


    }



    //abstracted in DoublyLinkedList
    public void getStateAtTime(int time){
        System.out.println(cpuScheduler.getStateAtTime(time) + " at time " + time);
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
        System.out.println("The Average Waiting Time for these process' for NonPreemptive Priority Algorithm is: " + sum / numOfKeys);
        System.out.println("----------------------------------");

    }




}
