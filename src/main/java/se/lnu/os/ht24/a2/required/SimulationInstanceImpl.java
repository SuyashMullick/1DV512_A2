package se.lnu.os.ht24.a2.required;

import se.lnu.os.ht24.a2.provided.abstract_.Instruction;
import se.lnu.os.ht24.a2.provided.data.ProcessInterval;
import se.lnu.os.ht24.a2.provided.data.StrategyType;
import se.lnu.os.ht24.a2.provided.exceptions.InstructionException;
import se.lnu.os.ht24.a2.provided.instructions.AllocationInstruction;
import se.lnu.os.ht24.a2.provided.instructions.CompactInstruction;
import se.lnu.os.ht24.a2.provided.instructions.DeallocationInstruction;
import se.lnu.os.ht24.a2.provided.interfaces.SimulationInstance;
import se.lnu.os.ht24.a2.required.MemoryImpl;

import java.util.*;

public class SimulationInstanceImpl implements SimulationInstance {
    private Queue<Instruction> remainingInstructions;
    private final MemoryImpl memory;
    private final StrategyType strategyType;
    private final List<InstructionException> instructionExceptions;

    public SimulationInstanceImpl(Queue<Instruction> instructions, MemoryImpl memory, StrategyType strategyType) {
        this.remainingInstructions = instructions;
        this.memory = memory;
        this.strategyType = strategyType;
        this.instructionExceptions = new ArrayList<>();
    }

    @Override
    public void runAll() {
        while (!remainingInstructions.isEmpty()) {
            executeNextInstruction();
        }
    }

    @Override
    public void run(int steps) {
        for (int i = 0; i < steps && !remainingInstructions.isEmpty(); i++) {
            executeNextInstruction();
        }
    }

    @Override
    public MemoryImpl getMemory() {
        return memory;
    }

    @Override
    public Queue<Instruction> getInstructions() {
        return remainingInstructions;
    }

    @Override
    public StrategyType getStrategyType() {
        return strategyType;
    }

    @Override
    public List<InstructionException> getExceptions() {
        return instructionExceptions;
    }

    private void executeNextInstruction() {
        Instruction instruction = remainingInstructions.poll();
        if (instruction == null) return;

        try {
            if (instruction instanceof AllocationInstruction) {
                allocateProcess((AllocationInstruction) instruction);
            } else if (instruction instanceof DeallocationInstruction) {
                deallocateProcess((DeallocationInstruction) instruction);
            } else if (instruction instanceof CompactInstruction) {
                compactMemory();
            }
        } catch (InstructionException e) {
            instructionExceptions.add(e);
        }
    }

    private void allocateProcess(AllocationInstruction instruction) throws InstructionException {
      int processId = instruction.getProcessId();
      int size = instruction.getDimension();
  
      // Attempt to allocate memory using the memory's allocate method
      boolean success = memory.allocate(processId, size, strategyType);
  
      if (!success) {
          // If allocation fails, throw an exception with the largest available free slot size
          int largestSlotSize = memory.freeSlots().stream()
                  .mapToInt(slot -> slot.getHighAddress() - slot.getLowAddress() + 1)
                  .max()
                  .orElse(0);
          throw new InstructionException(instruction, largestSlotSize);
      }
  }
  

  private void deallocateProcess(DeallocationInstruction instruction) throws InstructionException {
    int processId = instruction.getProcessId();

    // Attempt to deallocate memory
    boolean success = memory.deallocate(processId);

    if (!success) {
        // If deallocation fails, throw an exception
        throw new InstructionException(instruction, 0);
    }
}


private void compactMemory() {
  List<Integer> processes = memory.processes();

  for (int processId : processes) {
      ProcessInterval interval = memory.getProcessInterval(processId);
      int size = interval.getHighAddress() - interval.getLowAddress() + 1;

      // Deallocate the process from its current location
      memory.deallocate(processId);

      // Allocate the process using the FIRST_FIT strategy
      boolean success = memory.allocate(processId, size, StrategyType.FIRST_FIT);

      if (!success) {
          throw new IllegalStateException("Compact failed: Unable to reallocate process " + processId);
      }
  }
}



    private ProcessInterval selectSlot(List<ProcessInterval> freeSlots, int size, StrategyType strategyType) {
        switch (strategyType) {
            case FIRST_FIT:
                return freeSlots.stream()
                        .filter(slot -> slot.getHighAddress() - slot.getLowAddress() + 1 >= size)
                        .findFirst()
                        .orElse(null);
            case BEST_FIT:
                return freeSlots.stream()
                        .filter(slot -> slot.getHighAddress() - slot.getLowAddress() + 1 >= size)
                        .min(Comparator.comparingInt(slot -> slot.getHighAddress() - slot.getLowAddress() + 1))
                        .orElse(null);
            case WORST_FIT:
                return freeSlots.stream()
                        .filter(slot -> slot.getHighAddress() - slot.getLowAddress() + 1 >= size)
                        .max(Comparator.comparingInt(slot -> slot.getHighAddress() - slot.getLowAddress() + 1))
                        .orElse(null);
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "Simulation Details:\n" +
                "Strategy: " + strategyType + "\n" +
                "List of Remaining Instructions: " + remainingInstructions + "\n" +
                "Current Memory Structure:\n\n" + memory + "\n" +
                "List of Occurred Exceptions: " + instructionExceptions;
    }
}
