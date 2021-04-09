package assignment2.PreemptiveRoundRobin;


import java.util.ArrayList;
import java.util.HashMap;

import assignment2.DataStructures.DoublyLinkedList;
import assignment2.FCFS.IOScheduler;
import assignment2.ProcessHandler.Burst;
import assignment2.ProcessHandler.BurstType;
import assignment2.ProcessHandler.Process;
import assignment2.ProcessHandler.State;



public class PreemptiveRoundRobinSchedulerManager {
    private DoublyLinkedList cpuManager;		//used to manage bursts
    private DoublyLinkedList cpuScheduler;		//holds burst to output
    private HashMap<Integer, IOScheduler> ioSchedulerHashMap;		//holds bursts to output
    private final int TIMESLICE = 2;		//Time slice unique to round robin algorithm

    public PreemptiveRoundRobinSchedulerManager() {
        this.cpuManager = new DoublyLinkedList();
        this.cpuScheduler = new DoublyLinkedList();
        this.ioSchedulerHashMap = new HashMap<>();
    }

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


    //Adds processes to CPU scheduler and simulates execution according to the Round Robin algorithm
    public void addProcesses(ArrayList<Process> processes) {
        int timeExecutedSoFar = 0;
        Burst currBurst;
        Burst copyB;
        Process currProcess;

        //executes until all processes have terminated
        while(processes.size() > 0) {

            //this block sets processes with arrivalTime <= timeExecutedSoFar to READY
            for(int i= 0; i < processes.size(); i++) {
                if(processes.get(i).getArrivalTime() <= timeExecutedSoFar) {
                    if(processes.get(i).getState() == State.NEW) {
                        processes.get(i).setState(State.READY);									//signal process is ready to execute
                        cpuManager.addLast(processes.get(i).getBursts().removeFirst()); 		//adds burst to cpuManager since it is assumed to always be a cpu burst first
                    }																			//first burst is removed from a ready process
                }
            }

            //this block executes a burst on the cpu scheduler for the duration of one timeslice
            currBurst = cpuManager.removeFirst();			//remove first burst in cpu manager
            currProcess = currBurst.getProcessAssociated();
            currProcess.setState(State.RUNNING);
            if(currBurst.getStartTime() == 0)				//burst has not started before and preempted
                currBurst.setStartTime(timeExecutedSoFar);

            //case where cpu burst is less than or equal timeslice
            if(currBurst.getTime() <= TIMESLICE) {
                currBurst.setEndTime(timeExecutedSoFar + currBurst.getTime());
                copyB = Burst.deepClone(currBurst);			//adds the executed burst to the output cpu linked list
                cpuScheduler.addLast(copyB);
                //case where process has no next burst
                if(currProcess.getBursts().isEmpty()) {
                    currProcess.setState(State.TERMINATED);
                } else {
                    //process has a next burst
                    currBurst = currProcess.getBursts().getFirst();		//prev burst was removed, next burst should be in front
                    addBurstToScheduler(currBurst);
                    currProcess.setState(State.READY);
                }
                //case where cpu burst is greater than timeslice
            } else {
                //currBurst.setTimeLeft(TIMESLICE);				//simulate the burst running for 1 timeslice, duration reduced by timeslice
                copyB = Burst.deepClone(currBurst);
                copyB.setStartTime(timeExecutedSoFar);
                copyB.setEndTime(timeExecutedSoFar + TIMESLICE);
                cpuScheduler.addLast(copyB);
                currProcess.setState(State.WAITING);			//add a copy of the partial burst done to output cpuScheduler
                cpuManager.addLast(currBurst);					//preempt the burst back on the end of cpuManager
            }


            //executes burst on IO scheduler on each IO device
            //io scheduler treated as FCFS, bursts will not preempt
            //for loop iterates through all io device schedulers
            for(int i = 0; i < ioSchedulerHashMap.size(); i++) {
                //case where ioScheduler is not empty
                if((ioSchedulerHashMap.get(i).isEmpty()) == false) {
                    currBurst = ioSchedulerHashMap.get(i).getFirst().getBurst();
                    if(currBurst.getStartTime() == 0)
                        currBurst.setStartTime(timeExecutedSoFar);
                    //case where io burst is less than or equal timeslice
                    if(currBurst.getTime() <= TIMESLICE) {
                        currBurst.setEndTime(timeExecutedSoFar + (currBurst.getTime()));
                        ioSchedulerHashMap.get(i).removeFirst();
                        //case where io burst greater than timeslice
                    } else {
                        //currBurst.setTimeLeft(TIMESLICE);
                    }

                }
            }

            timeExecutedSoFar += TIMESLICE;		//increment the time by timeslice

        }
        cpuScheduler.printAllBursts("----CPUScheduler Device 0 -----");
        for (int i : ioSchedulerHashMap.keySet()) {
            ioSchedulerHashMap.get(i).printAllBursts("---IOScheduler Device " + i + "---");
        }

    }

    //abstracted in DoublyLinkedList
    public void getStateAtTime(int time){
        System.out.println(cpuScheduler.getStateAtTime(time) + " at time " + time);
    }

    public double averageWaitingTime(){
        HashMap<Integer, Integer> totalTimeWaiting = new HashMap<>(); //key is ProcessID, value is totalTime that Process is in waiting queue
        for (IOScheduler scheduler : ioSchedulerHashMap.values()){
            DoublyLinkedList.Node curr = scheduler.getFirst();
            while (curr.getBurst() != null){
                int processId = curr.getBurst().getProcessAssociated().getProcessId();
                int timeAlreadyCalculated = totalTimeWaiting.getOrDefault(processId, 0);
                timeAlreadyCalculated+=curr.getBurst().getTime();
                totalTimeWaiting.put(processId, timeAlreadyCalculated);
                curr = curr.getNext();
            }
        }
        int totalSum = 0;
        int amount = 0;
        for (int key : totalTimeWaiting.keySet()){
            totalSum+=totalTimeWaiting.get(key);
            amount++;
        }
        return totalSum/amount;
    }

    public double averageTurnAroundTime() {			//MUST IMPLEMENT
        return 0.0;
    }


}
