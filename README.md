# CPU-and-I-O-Scheduler-Algorithms

In this project, we have designed and implemented a system for simulating the deterministic modelling for CPU Scheduling. There are 6 different scheduling algorithms that we have implemented and the Pre-emptive algorithms also include I/O device scheduler for multiple devices while also handling the case for piplelined processes. We managed to do this by checking the certain condition for priority/time left for completion for each burst within a process after every single clock cycle so as to choose the best possible candidate.

We have also included methods to calculate the waiting time and the turnaround time for each process and thus calculate the average waiting time and turnaround time from that. Lastly, we have also implemented a method whih prints out the gantt-chart representing the whole cycle for the processes and their bursts in order to get a visual representation of what goes down. Further details on how to execute the algorithms are explained below.


How to Execute and setup custom process'
------------------------------------------
Note 1: How to run and what everything means

Navigate to src/assignment2/main.java

In the main.java file you will see a strict layout of how any process and any and all algorithms should be run.
In the main.java file we have included an example to follow.

At the top of the main method you will see 5 instances of objects. These are all instances of the algorithms.
a. fcfs = First Come First Serve
b. nonSJF = Non Preemptive Shortest Job First
c. nonPPriority = Non Preemptive Priority
d. preemptiveSJF = Preemptive Shortest Job First
e. preemptivePriority = Preemptive Priority

Below this you will see 3 Process objects, these 3 are there as an example.
- Each Process has a processId (e.g. object p1 process id is 1, p2 is 2 etc.)
- Each Process has an arrivalTime parameter (self explanatory)
- Each Process also has a priority (self explanatory)


For each process there is multiple .addBurst calls
- these addBurst calls add a new instance of a burst that is associated with the process.
- For Each Burst there is the following
  - deviceId :(If it is a CPU Burst it will default to deviceId 0 but put 0 just in case, for I_O Burst put whatever device u want it to run on)
  - time : this is the burst time
  - bursttype : Either BurstType.CPU or BurstType.I_O


 After you have created each Process and its respective bursts please make sure
 to add each Process object to the processes ArrayList below.

 Finally, select the algorithm object reference you want, in the example we chose preemptivePriority
 and use that to call .addProcesses(processes) - THIS MUST COME first

 Then you can call ganttChart() which will output to stdout the ganttChart equivalent
 averageWaitingTime()
 averageTurnAroundTime()




Note 2: 
./output_tests
- In this folder we have added the output from our testing each algorithm to show that each algorithm works properly. The output
is using the Process info provided in the src you will see in Main.java. The burst and process times in main are exactly the same as the output test

Note 3:
ganttChart()
The output of the gantt chart may look like the following for example
----CPUScheduler Device 0 -----
	0|4- P1
	---
	7|9- P1
	---
	9|10- P2


What this means is that from time 0 to 4, process P1 was running in CPU Scheduler Device 0
from 7 to 9 process p1 was running in device scheduler device 0
and from 9 to 10 process p2 was running in device scheduler device 1

This pattern is sustained across the CPU Scheduler and all IOScheduler gantt charts
