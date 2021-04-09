package assignment2.FCFS;

import assignment2.DataStructures.DoublyLinkedList;

//IOScheduler is just FCFS so we'll extend it and add one extra method
public class IOScheduler extends DoublyLinkedList {
    private int id;
    public void setId(int id){
        this.id = id;
    }
}
