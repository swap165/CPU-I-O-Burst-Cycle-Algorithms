package assignment2.PreemptiveSJF;

import assignment2.DataStructures.DoublyLinkedList;
import assignment2.FCFS.IOScheduler;
import assignment2.ProcessHandler.Burst;
import assignment2.ProcessHandler.BurstType;
import assignment2.ProcessHandler.Process;

import java.util.*;

public class PreemptiveSJF {
    private HashMap<Integer, IOScheduler> ioSchedulerHashMap;
    private DoublyLinkedList cpuScheduler;
    private HashMap<Integer, HashMap<Integer, LinkedList<Process>>> waitingRoomSnapShots;

    public PreemptiveSJF() {
        this.ioSchedulerHashMap = new HashMap<>();
        this.cpuScheduler = new DoublyLinkedList();
        this.waitingRoomSnapShots = new HashMap<>();
    }

    //Algorithms to compute average waiting time
        //How it works:
            /*
                Loops through the entire gantt chart for each process and check checks each clock cycle
                If a process is not executing on the CPU or any IODevice time at that clock cycle and it is not in an IOQueue then it is considered waiting so we add 1 time
                The complexity of this is knowing when it is in the IOQueue. To solve this problem we just memoized each clock cycle in the addProcesses algorithm which runs the equivalent
                algorithm, e.g. PreemptiveSJF
             */
    public double averageWaitingTime(){
        //FIRST FIND THE ENDTIMES OF EACH PROCESS
        HashMap<Process, Integer> endTimes = new HashMap<>();
        DoublyLinkedList.Node currNode = cpuScheduler.getLast();

        while (currNode != null && currNode.getBurst() != null){
            if (endTimes.get(currNode.getBurst().getProcessAssociated()) == null && currNode.getBurst() != null){
                endTimes.put(currNode.getBurst().getProcessAssociated(), currNode.getBurst().getEndTime());
            }
            currNode = currNode.getPrev();
        }



        HashMap<Integer, Integer> waitingTimeForProcesses = new HashMap<>();


        for (Process p : endTimes.keySet()){
            int arrivalTime = p.getArrivalTime();
            int endTime = endTimes.get(p);

            currNode = cpuScheduler.getFirst();
            //currNode needs to catch up
            while (currNode.getBurst().getStartTime() != arrivalTime){
                currNode = currNode.getNext();
            }


            int begin = arrivalTime;


            waitingTimeForProcesses.put(p.getProcessId(), 0);
            while (begin <= endTime && currNode.getBurst() != null){
                if (currNode == null || currNode.getBurst() == null) continue;

                if (begin < arrivalTime){
                    currNode = currNode.getNext();
                    begin = currNode.getBurst().getStartTime();
                    continue;
                }

                if (begin >= endTime){
                    break;
                }

                if (begin == 13 && p.getProcessId() == 3){
                    System.out.println("");
                }

                if (currNode.getBurst().getProcessAssociated() == p && currNode.getBurst().getStartTime() == begin){
                    if (currNode == null || currNode.getNext() == null){
                        continue;
                    }
                    currNode = currNode.getNext();
                    if (currNode.getBurst() != null) {
                        begin = currNode.getBurst().getStartTime();
                        continue;
                    }
                }

                //loop through each iodevice
                boolean moveOn = false;
                for (int ioDevice : ioSchedulerHashMap.keySet()){
                    IOScheduler s = ioSchedulerHashMap.get(ioDevice);
                    //loop through each node in IOScheduler

                    DoublyLinkedList.Node schedulerNode = s.getFirst();
                    if (schedulerNode.getBurst() == null) continue;
                    while (schedulerNode != null && schedulerNode.getBurst() != null && schedulerNode.getBurst().getStartTime() <= begin){
                        if (schedulerNode.getBurst().getEndTime() >= begin && schedulerNode.getBurst().getStartTime() <= begin){
                            if (schedulerNode.getBurst().getProcessAssociated() == p) {

                                if (currNode.getBurst() != null && currNode != null && schedulerNode != null && schedulerNode.getBurst() != null) {
                                    int timeSoFarCalculated = waitingTimeForProcesses.get(p.getProcessId());
                                    if (currNode.getBurst().getEndTime() > schedulerNode.getBurst().getEndTime())
                                        waitingTimeForProcesses.put(p.getProcessId(), currNode.getBurst().getEndTime() - schedulerNode.getBurst().getEndTime() + timeSoFarCalculated);
                                }
                                moveOn = true;
                                break;
                            }
                        }
                        schedulerNode = schedulerNode.getNext();

                    }
                    if (moveOn){
                        if (currNode == null || currNode.getNext() == null){
                            continue;
                        }


                        currNode = currNode.getNext();

                        begin = currNode.getBurst().getStartTime();
                        break;
                    }
                }

                if (moveOn)
                    continue;



                //loop through the required waiting room
                HashMap<Integer, LinkedList<Process>> waitingRoomAtThisTime = this.waitingRoomSnapShots.get(begin);
                if (waitingRoomAtThisTime != null) {
                    for (Integer i : waitingRoomAtThisTime.keySet()) {
                        if (waitingRoomAtThisTime.get(i) == null) {
                            continue;
                        }
                        LinkedList<Process> room = waitingRoomAtThisTime.get(i);
                        for (Process proccess : room) {
                            if (proccess == p) {
                                moveOn = true;
                                break;
                            }
                        }
                        if (moveOn) break;
                    }
                }else if (waitingRoomAtThisTime == null && begin != 0){
                    moveOn = true;
                }

                if (moveOn){
                    currNode = currNode.getNext();
                    begin = currNode.getBurst().getStartTime();
                    continue;
                }




                //else condition so means its waiting
                int timeSoFarCalculated = waitingTimeForProcesses.getOrDefault(p.getProcessId(), 0);

                if (currNode.getBurst() != null) {
                    timeSoFarCalculated += currNode.getBurst().getEndTime() - currNode.getBurst().getStartTime();
                    waitingTimeForProcesses.put(p.getProcessId(), timeSoFarCalculated);

                    currNode = currNode.getNext();
                    if (currNode.getBurst() != null){
                        begin = currNode.getBurst().getStartTime();
                    }

                }

            }
        }

        double sum = 0;
        double numOfKeys = 0;
        System.out.println("The Individual Waiting Time for Each Process is... ");
        for (int key : waitingTimeForProcesses.keySet()){
            numOfKeys++;
            sum+= waitingTimeForProcesses.get(key);
            System.out.println("-Process Id: " + key + " Has an individual waiting time of " + waitingTimeForProcesses.get(key));
        }
        System.out.println("----------------------------------");
        System.out.println("The Average Waiting Time for these process' for Preemptive SJF Algorithm is: " + sum / numOfKeys);
        System.out.println("----------------------------------");
        return sum / numOfKeys;
    }

    //Simply calculates when the clock cycle time for which the process terminates minus arrival time
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
        System.out.println("The Average Turnaround Time for these process' for Preemptive Priority Algorithm is: " + sum / numOfKeys);
        System.out.println("----------------------------------");

    }

    //Prints out ganttChart for the CPU and all IOSchedulers after the algorithm is run
    public void ganttChart() {
        cpuScheduler.printAllBursts("----CPUScheduler Device 0 -----");
        for (int i : ioSchedulerHashMap.keySet()) {
            ioSchedulerHashMap.get(i).printAllBursts("---IOScheduler Device " + i + "---");
        }

    }

    //This algorithm adds a process burst to the cpuscheduler if its burst is the shortest. When a burst becomes 0 it adds the IOBurst that comes after it to a IOSchedulerWaitingRoom
    //On each clock cycle it checks if the IOScheduler is free and removes the first thing from the IOScheduler waiting room and keeps going
    public void addProcesses(ArrayList<Process> processes) {
        HashMap<Integer, Process> IOProcessWorking = new HashMap<>(); //key : device, value : Process
        HashMap<Integer, LinkedList<Process>> IOSchedulerWaitingRoom = new HashMap<>(); //Queue no details in the queue, except for process itself
        ArrayList<Process> anythingBusy = new ArrayList<>(); //union of IOProcessWorking and IOSchedulerWaitingRoom

        int clock = 0;
    boolean emptyTime = false;


        Burst prevBurst = null;


        while (processes.size() > 0) {

            if (clock == 9){
                System.out.println("");
            }



            Iterator it = IOProcessWorking.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                int deviceID = (int) pair.getKey();
                Process process = (Process) pair.getValue();

                if (clock >= process.getBursts().getFirst().getEndTime()){
                    it.remove();
                    process.getBursts().remove(0);


                    ListIterator<Process> itr = anythingBusy.listIterator();
                    while (itr.hasNext()){
                        Process procc = itr.next();
                        if (procc == process){
                            itr.remove();
                        }
                    }
                }
            }


            //Add something to IOProcessWorking if possible
            for (int deviceWaitingFor : IOSchedulerWaitingRoom.keySet()){
                if (IOProcessWorking.get(deviceWaitingFor) == null){
                    if (IOSchedulerWaitingRoom.get(deviceWaitingFor) == null || IOSchedulerWaitingRoom.get(deviceWaitingFor).size() == 0){
                        continue;
                    }
                    IOSchedulerWaitingRoom.get(deviceWaitingFor).getFirst().getBursts().getFirst().setStartTime(clock);
                    IOSchedulerWaitingRoom.get(deviceWaitingFor).getFirst().getBursts().getFirst().setEndTime(clock + IOSchedulerWaitingRoom.get(deviceWaitingFor).getFirst().getBursts().getFirst().getTime());
                    Burst p = IOSchedulerWaitingRoom.get(deviceWaitingFor).getFirst().getBursts().getFirst();
                    addBurstToScheduler(p);
                    IOProcessWorking.put(deviceWaitingFor, IOSchedulerWaitingRoom.get(deviceWaitingFor).getFirst());
                    IOSchedulerWaitingRoom.get(deviceWaitingFor).remove(0);
                }
            }


            ArrayList<Process> bestProcessCandidates = new ArrayList<>();
            for (Process p : processes){
                if (p.getArrivalTime() <= clock && !anythingBusy.contains(p)){
                    bestProcessCandidates.add(p);
                }
            }

            Process bestProcess = null;

            for (Process p : bestProcessCandidates){
                if (bestProcess == null)
                    bestProcess = p;
                else if (p.getBursts().getFirst().getTime() < bestProcess.getBursts().getFirst().getTime()){
                    bestProcess = p;
                }else if (p.getBursts().getFirst().getTime() == bestProcess.getBursts().getFirst().getTime()){
                    bestProcess = bestProcess.getProcessId() < p.getProcessId() ? bestProcess : p;
                }
            }

            if (bestProcess == null){
                clock++;
                emptyTime = true;
                continue;
            }

            Burst currBurst = Burst.deepClone(bestProcess.getBursts().getFirst());

            if (prevBurst == null){
                currBurst.setStartTime(0);
                currBurst.setEndTime(1);
            }else {
                //if prevburst == currburst, then update prevburst time reference
                if (currBurst.equals(prevBurst)) {
                    cpuScheduler.getLast().getBurst().setEndTime(cpuScheduler.getLast().getBurst().getEndTime() + 1);
                    currBurst.setStartTime(cpuScheduler.getLast().getBurst().getStartTime());
                    currBurst.setEndTime(cpuScheduler.getLast().getBurst().getEndTime());

                } else {
                    currBurst.setStartTime(prevBurst.getEndTime());
                    currBurst.setEndTime(currBurst.getStartTime() + 1);

                    if (emptyTime == true){
                        currBurst.setStartTime(clock);
                        currBurst.setEndTime(currBurst.getStartTime() + 1);
                    }

                    emptyTime = false;
                }
            }


            if (prevBurst == null || !prevBurst.equals(currBurst))
                addBurstToScheduler(currBurst);

            bestProcess.getBursts().getFirst().removeTime(1);
            currBurst.removeTime(1);




            if (currBurst.getTime() == 0 && bestProcess.getBursts().size() > 1 && bestProcess.getBursts().get(1).getBurstType() == BurstType.I_O){
                int deviceIDForIO = bestProcess.getBursts().get(1).getDeviceId();
                LinkedList<Process> waitingRoomForTheDevice = IOSchedulerWaitingRoom.getOrDefault(deviceIDForIO, new LinkedList<>());
                waitingRoomForTheDevice.add(bestProcess);
                IOSchedulerWaitingRoom.put(deviceIDForIO, waitingRoomForTheDevice);
                anythingBusy.add(bestProcess);
            }


            //Check if burstDuration == 0. If so, remove it from process
            if (bestProcess.getBursts().getFirst().getTime() == 0)
                bestProcess.getBursts().remove(0);

            //Check if # OF BURSTS IN A PROCESS = 0, remove process from processses list
            if (bestProcess.getBursts().size() == 0){
                processes.remove(bestProcess);
            }
            prevBurst = currBurst;
            clock++;
            HashMap<Integer, LinkedList<Process>> IOSchedulerWaitingRoomClone = new HashMap<>();
            for (int i : IOSchedulerWaitingRoom.keySet()){
                LinkedList<Process> copy = new LinkedList<>();
                for (Process p : IOSchedulerWaitingRoom.get(i)){
                    copy.add(p);
                }
                IOSchedulerWaitingRoomClone.put(i, copy);
            }
            this.waitingRoomSnapShots.put(clock, IOSchedulerWaitingRoomClone);

        }
    }




    //Helper method used to add a Burst to its associated scheduler
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



}
