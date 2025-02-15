package se.lnu.os.ht24.a2.required;

import se.lnu.os.ht24.a2.provided.data.ProcessInterval;
import se.lnu.os.ht24.a2.provided.data.StrategyType;
import se.lnu.os.ht24.a2.provided.interfaces.Memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemoryImpl implements Memory {

  private final int size;
  private final int[] memory;
  private final Map<Integer, ProcessInterval> processMap;

  public MemoryImpl(int size) {
    this.size = size;
    this.memory = new int[size];
    Arrays.fill(memory, -1);  // initialize memory with -1, implying that the block is empty
    this.processMap = new HashMap<>();
  }

  @Override
  public boolean containsProcess(int processId) {
    return processMap.containsKey(processId);
  }

  @Override
  public List<Integer> processes() {
    return new ArrayList<>(processMap.keySet());
  }

  @Override
  public int processSize(int processId) {
    ProcessInterval interval = processMap.get(processId);
    if (interval == null) {
      return 0; // Process not allocated
    }
    return interval.getHighAddress() - interval.getLowAddress() + 1; // Size calculation
  }

  @Override
  public ProcessInterval getProcessInterval(int processId) {
    return processMap.get(processId);
  }

  @Override
  public Set<Integer> neighboringProcesses(int processId) {
    // Initialize an empty set to store the neighboring processes ID's
    Set<Integer> neighbors = new HashSet<>();

    // return an empty set if the process ID does not exist.
    if (!containsProcess(processId))
      return neighbors;

    // get the memory interval of the given process
    ProcessInterval interval = processMap.get(processId);
    int low = interval.getLowAddress();
    int high = interval.getHighAddress();

    // check if there is a process to the left of the given process
    if (low > 0 && memory[low - 1] != -1) {
      neighbors.add(memory[low - 1]); // add to set if exists
    }

    // check if there is a process to the right of the given process
    if (high < memory.length - 1 && memory[high + 1] != -1) {
      neighbors.add(memory[high + 1]);// add to set if exists
    }

    // return the set of neighboring processes
    return neighbors;
  }

  @Override
  public double fragmentation() {
    // Initialize a list to store the sizes of free blocks
    List<Integer> freeBlocks = new ArrayList<>();
    
    // Initialize a variable to keep traclk of the current block size
    int currentBlockSize = 0;

    // Iterate through each slot in the memory
    for (int slot : memory) {
      // If slot is free, increment the currentBlockSize
      if (slot == -1) {
        currentBlockSize++;
      }
      
      // If slot is not free, add the currentBlockSize to the freeBlocks list and reset the currentBlockSize
      else if (currentBlockSize > 0) {
        freeBlocks.add(currentBlockSize);
        currentBlockSize = 0;
      }
    }

    // If the last slot was free, add the currentBlockSize to the freeBlocks list
    if (currentBlockSize > 0)
      freeBlocks.add(currentBlockSize);

    // If there are no free blocks, return 0 (no fragmentation)
    if (freeBlocks.isEmpty())
      return 0.0;

    // Find the largest free block
    int largestBlock = Collections.max(freeBlocks);

    // Calculate the total amount of free memory
    int totalFreeMemory = freeBlocks.stream().mapToInt(Integer::intValue).sum();

    // Return the fragmentation value
    return 1.0 - ((double) largestBlock / totalFreeMemory);
  }

  @Override
  public Set<ProcessInterval> freeSlots() {
    // Initialize a set to store the free slots
    Set<ProcessInterval> freeSlots = new HashSet<>();

    if (memory == null || memory.length == 0) {
      return freeSlots;
    }
    // free variable to keep track of the current free slot
    int start = -1;

    // Iterate through each slot in the memory
    for (int i = 0; i < memory.length; i++) {
      // If the slot is free (-1), check if it's the start of a new free slot
      if (memory[i] == -1) {
        // If it's the start of a new free slot, update the start variable
        if (start == -1)
          start = i; // Start of a free slot.
      }

      // If the slot is not free, and we were previously in a free slot,
      // add the current free slot to the set and reset the start variable.
      else {
        if (start != -1) {
          freeSlots.add(new ProcessInterval(start, i - 1));
          start = -1;
        }
      }
    }

    // If there is a free slot at the end of the memory, add it to the set
    if (start != -1) {
      freeSlots.add(new ProcessInterval(start, memory.length - 1));
    }

    return freeSlots;
  }

  public boolean allocate(int processId, int size, StrategyType strategyType) {
    if (containsProcess(processId) || size <= 0 || size > memory.length) {
      return false; // Invalid request or process already exists.
    }

    // Find free slots
    List<ProcessInterval> freeSlots = new ArrayList<>(freeSlots());
    ProcessInterval selectedSlot = selectSlot(freeSlots, size, strategyType);

    if (selectedSlot == null) {
      return false; // No suitable slot found for allocation.
    }

    // Allocate memory
    int start = selectedSlot.getLowAddress();
    int end = start + size - 1;

    if (start < 0 || end >= memory.length) {
      return false;             // handles out-of-bounds memory access.
    }

    for (int i = start; i < start + size; i++) {
      memory[i] = processId;
    }

    // Update process map
    processMap.put(processId, new ProcessInterval(start, end));
    return true;
  }

  public boolean deallocate(int processId) {
    if (!containsProcess(processId)) {
      return false; // Process not found.
    }

    // Deallocate memory
    ProcessInterval interval = processMap.get(processId);
    if (interval == null) {
      return false;             // safety check for null pointer exception.
    }
    for (int i = interval.getLowAddress(); i <= interval.getHighAddress(); i++) {
      memory[i] = -1;
    }

    // Remove from process map
    processMap.remove(processId);
    return true;
  }

  // Helper method to select a slot based on strategy
  private ProcessInterval selectSlot(List<ProcessInterval> freeSlots, int size, StrategyType strategyType) {
    switch (strategyType) {
      case FIRST_FIT:
        return freeSlots.stream()
            // Filter out slots that are smaller than the requested size
            .sorted(Comparator.comparingInt(ProcessInterval::getLowAddress))
            .filter(slot -> slot.getHighAddress() - slot.getLowAddress() + 1 >= size)
            .findFirst() // Find the first free slot that meets the size requirement
            .orElse(null); // Return null if no suitable slot is found
      case BEST_FIT:
        return freeSlots.stream()
            .filter(slot -> slot.getHighAddress() - slot.getLowAddress() + 1 >= size)
            // sort the free slots by size and select the smallest one
            .min(Comparator.comparingInt(slot -> slot.getHighAddress() - slot.getLowAddress() + 1))
            .orElse(null);
      case WORST_FIT:
        return freeSlots.stream()
            .filter(slot -> slot.getHighAddress() - slot.getLowAddress() + 1 >= size)
            // sort the free slots by size in desceding order and select the largest one
            .max(Comparator.comparingInt(slot -> slot.getHighAddress() - slot.getLowAddress() + 1))
            .orElse(null);
      default:
        return null; // In case of an unsupported strategy type.
    }
  }

  @Override
  public String toString() {
    StringBuilder retStr = new StringBuilder("Memory Size = " + size + "\n");
    if (processes() != null) {
      for (int processId : processes()) {
        ProcessInterval inter = getProcessInterval(processId);
        retStr.append("(").append(inter.getLowAddress()).append("-").append(inter.getHighAddress()).append(")")
            .append(" --> ").append("ID ").append(processId).append("\n");
      }
    }
    if (freeSlots() != null) {
      for (ProcessInterval bi : freeSlots()) {
        retStr.append("(").append(bi.getLowAddress()).append("-").append(bi.getHighAddress()).append(")")
            .append(" --> ").append("EMPTY").append("\n");
      }
    }
    return retStr.toString();
  }
}
