package assignment2.DataStructures;

import assignment2.ProcessHandler.Burst;
import assignment2.ProcessHandler.Process;
import assignment2.ProcessHandler.State;


//Linked List Implementation Modified from Textbook. We Don't use Node<E> here because we know the Node will own a type of Burst so generic <E> is unnecessary. Hence just Node
public class DoublyLinkedList {
    public static class Node {
        private Node prev;
        private Node next;
        private Burst burst;

        public Node(Burst b, Node p, Node n){
            this.burst = b;
            this.prev = p;
            this.next = n;
        }

        public Burst getBurst(){return burst;}
        public Node getPrev(){return this.prev;}
        public Node getNext(){return this.next;}
        public void setNext(Node b){this.next = b;}
        public void setPrev(Node b) {this.prev = b;}
    }

    private Node header;
    private Node trailer;
    private int size = 0;

    public DoublyLinkedList(){
        header = new Node(null, null, null);
        trailer = new Node(null, header, null);
        header.setNext(trailer);
    }

    public int size(){return size;}
    public boolean isEmpty(){return size == 0;}

    public Burst first(){
        if (this.isEmpty()) return null;
        return header.getNext().getBurst();
    }

    public Node getFirst(){
        return header.getNext();
    }

    public Node getLast(){
        return trailer.getPrev();
    }





    public Burst last(){
        if (this.isEmpty()) return null;
        return trailer.getPrev().getBurst();
    }

    public void addFirst(Burst b) {
        addBetween(b, header, header.getNext());
    }

    public void addLast(Burst b){
        addBetween(b, trailer.getPrev(), trailer);
    }

    public Burst removeFirst(){
        if (isEmpty()) return null;
        return remove(header.getNext());
    }

    public Burst removeLast(){
        if (isEmpty()) return null;
        return remove(trailer.getPrev());
    }

    private Burst remove(Node node){
        Node predecessor = node.getPrev();
        Node sucessor = node.getNext();
        predecessor.setNext(sucessor);
        predecessor.setPrev(predecessor);
        size--;
        return node.getBurst();
    }

    private void addBetween(Burst b, Node predecessor, Node successor){
        Node newest = new Node(b, predecessor, successor);
        predecessor.setNext(newest);
        successor.setPrev(newest);
        size++;
    }


    //if time is 0 then it's on the edge of ready and running but we'll call it ready because it hasn't offically started running so we call it Ready
    //If time > trailer.getPrev().getEndTime() then process is terminated
    //if time exists within the bounds of a process then it's running
    //if time does not exist within the bounds of a process then its waiting
    public State getStateAtTime(int time){
        if (time == 0) return State.READY;
        if (time >= trailer.getPrev().getBurst().getEndTime()) return State.TERMINATED;

        Node curr = header.getNext();

        while (curr != trailer){
            if (time < curr.getBurst().getEndTime() && time >= curr.getBurst().getStartTime())
                return State.RUNNING;
            curr = curr.getNext();
        }

        return State.WAITING;
    }

    public void printAllBursts(String optional){
        Node curr = header.getNext();
        System.out.println(optional);
        while (curr != trailer){
            System.out.println("\t" + curr.burst.getStartTime() + "|" + curr.burst.getEndTime() + "- P" + curr.burst.getProcessAssociated().getProcessId());
            System.out.println("\t---");
            curr = curr.getNext();
        }
        System.out.println("---Finished Output---\n");

    }


}
