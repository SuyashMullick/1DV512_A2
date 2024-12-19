package se.lnu.os.ht24.a2.required;

import se.lnu.os.ht24.a2.provided.data.ProcessInterval;
import se.lnu.os.ht24.a2.provided.interfaces.Memory;

import java.util.List;
import java.util.Set;

public class MemoryImpl implements Memory {

    private final int size;

    public MemoryImpl(int size){
        /* TODO
            Structure your memory how you like and initialize it here. This is the only constructor allowed and
            should create an empty memory of the given size. Feel free to add any variable or method you see
            fit for your implementation in this class
         */
        this.size = size;
    }

    @Override
    public boolean containsProcess(int processId) {
        // TODO Replace this return statement with the method that checks if processId is allocated in the memory
        return false;
    }

    @Override
    public List<Integer> processes() {
        /* TODO
            Replace this return statement with the list of processIds of the currently allocated processes
            in the memory. If the memory is empty, return an empty List.
         */
        return null;
    }

    @Override
    public int processSize(int processId) {
        /* TODO
            Replace this return statement with the method that returns the size of the process with processId
            in the memory, 0 if it is not allocated.
         */
        return 0;
    }

    @Override
    public ProcessInterval getProcessInterval(int processId) {
        /* TODO
            Replace this return statement with the method that returns a ProcessInterval instance containing the
            lower and upper address in memory of the process with processId. Return null if the process is not allocated
         */
        return null;
    }

    @Override
    public Set<Integer> neighboringProcesses(int processId) {
        /* TODO
            Replace this return statement with the method that returns the Set containing the ids of all the
            contiguous processes to the one that has processId (min. 0 if the process is between two free portions of
            memory and max. 2 if the process is surrounded both left and right by other processes). For no neighboring
            processes, return an empty Set.
         */
        return null;
    }

    @Override
    public double fragmentation() {
        /* TODO
            Replace this return statement with the method that returns the memory fragmentation value. There is
            no need to round decimals, as the Tests will do it before checking.
         */
        return 0;
    }

    @Override
    public Set<ProcessInterval> freeSlots() {
        /* TODO
            Replace this return statement with the method that returns the set of ProcessInterval instances
            corresponding to the free slots of the memory. Return exactly one ProcessInterval per slot, make sure
            that you don't split any slot in two different intervals (e.g. if slot 0-199 is free, adding 0-99
            and 100-199 will be considered an error, while adding 0-199 is the only correct solution). If the
            memory is full, return an empty Set.
         */
        return null;
    }

    @Override
    public String toString() {
        StringBuilder retStr = new StringBuilder("Memory Size = " + size + "\n");
        if(processes() != null) {
            for (int processId : processes()) {
                ProcessInterval inter = getProcessInterval(processId);
                retStr.append("(").append(inter.getLowAddress()).append("-").append(inter.getHighAddress()).append(")")
                        .append(" --> ").append("ID ").append(processId).append("\n");
            }
        }
        if(freeSlots() != null) {
            for (ProcessInterval bi : freeSlots()) {
                retStr.append("(").append(bi.getLowAddress()).append("-").append(bi.getHighAddress()).append(")")
                        .append(" --> ").append("EMPTY").append("\n");
            }
        }
        return retStr.toString();
    }
}
