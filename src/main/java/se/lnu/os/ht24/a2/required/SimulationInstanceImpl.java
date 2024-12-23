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

  // The list of exceptions that occuerd during the execution of instructions.
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
    // loop through the specified number of steps, or until there are no more instructions to execute
    for (int i = 0; i < steps && !remainingInstructions.isEmpty(); i++) {
      // execute the next instruction in the queue
      executeNextInstruction();
    }
  }

  @Override
  public MemoryImpl getMemory() {
    return this.memory;
  }

  @Override
  public Queue<Instruction> getInstructions() {
    return this.remainingInstructions;
  }

  @Override
  public StrategyType getStrategyType() {
    return this.strategyType;
  }

  @Override
  public List<InstructionException> getExceptions() {
    return this.instructionExceptions;
  }

  private void executeNextInstruction() {
    // get next instruction from the queue
    Instruction instruction = remainingInstructions.poll();

    // If queue is empty, do nothing and return
    if (instruction == null)
      return;

    try {
      // check the type of instrucction and execute the methods accordingly
      if (instruction instanceof AllocationInstruction) {
        allocateProcess((AllocationInstruction) instruction);
      } else if (instruction instanceof DeallocationInstruction) {
        deallocateProcess((DeallocationInstruction) instruction);
      } else if (instruction instanceof CompactInstruction) {
        // compact the memory to remove any free slots
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
        // If deallocation fails, throw an exception with the largest free slot size
        int largestSlotSize = memory.freeSlots().stream()
            .mapToInt(slot -> slot.getHighAddress() - slot.getLowAddress() + 1)
            .max()
            .orElse(0); // Default to 0 if no slots are available
        throw new InstructionException(instruction, largestSlotSize);
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

  @Override
  public String toString() {
    return "Simulation Details:\n" +
        "Strategy: " + strategyType + "\n" +
        "List of Remaining Instructions: " + remainingInstructions + "\n" +
        "Current Memory Structure:\n\n" + memory + "\n" +
        "List of Occurred Exceptions: " + instructionExceptions;
  }
}
