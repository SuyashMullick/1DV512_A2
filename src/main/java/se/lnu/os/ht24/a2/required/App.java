package se.lnu.os.ht24.a2.required;

import java.util.ArrayDeque;
import java.util.Queue;

import se.lnu.os.ht24.a2.provided.abstract_.Instruction;
import se.lnu.os.ht24.a2.provided.data.StrategyType;
import se.lnu.os.ht24.a2.provided.instructions.AllocationInstruction;
import se.lnu.os.ht24.a2.provided.instructions.DeallocationInstruction;
import se.lnu.os.ht24.a2.provided.instructions.CompactInstruction;

public class App {

  public static void main(String[] args) {

    int[] processSizeList = new int[4];
    processSizeList[0] = 2;
    processSizeList[1] = 4;
    processSizeList[2] = 2;
    processSizeList[3] = 2;

    int memorySize = 10;

    Queue<Instruction> firstFitQueue = new ArrayDeque<>();
    firstFitQueue.add(new AllocationInstruction(1, processSizeList[0]));
    firstFitQueue.add(new AllocationInstruction(2, processSizeList[1]));
    firstFitQueue.add(new AllocationInstruction(3, processSizeList[2]));
    firstFitQueue.add(new DeallocationInstruction(2));
    firstFitQueue.add(new AllocationInstruction(4, processSizeList[3]));
/*     firstFitQueue.add(new CompactInstruction());
 */
    Queue<Instruction> bestFitQueue = new ArrayDeque<>();
    bestFitQueue.add(new AllocationInstruction(1, processSizeList[0]));
    bestFitQueue.add(new AllocationInstruction(2, processSizeList[1]));
    bestFitQueue.add(new AllocationInstruction(3, processSizeList[2]));
    bestFitQueue.add(new DeallocationInstruction(2));
    bestFitQueue.add(new AllocationInstruction(4, processSizeList[3]));
/*     bestFitQueue.add(new CompactInstruction());
 */
    Queue<Instruction> worstFitQueue = new ArrayDeque<>();
    worstFitQueue.add(new AllocationInstruction(1, processSizeList[0]));
    worstFitQueue.add(new AllocationInstruction(2, processSizeList[1]));
    worstFitQueue.add(new AllocationInstruction(3, processSizeList[2]));
    worstFitQueue.add(new DeallocationInstruction(2));
    worstFitQueue.add(new AllocationInstruction(4, processSizeList[3]));
/*     worstFitQueue.add(new CompactInstruction());
 */
    System.out.println("instruction queue size: " + firstFitQueue.size());

    SimulationInstanceImpl firstFitInst = new SimulationInstanceImpl(firstFitQueue, new MemoryImpl(memorySize),
        StrategyType.FIRST_FIT);

    SimulationInstanceImpl bestFitInst = new SimulationInstanceImpl(bestFitQueue, new MemoryImpl(memorySize),
        StrategyType.BEST_FIT);

    SimulationInstanceImpl worstFitInst = new SimulationInstanceImpl(worstFitQueue, new MemoryImpl(memorySize),
        StrategyType.WORST_FIT);

    firstFitInst.runAll();
    System.out.println("final memory state of first fit: " + firstFitInst.getMemory());

    bestFitInst.runAll();
    System.out.println("final memory state of best fit: " + bestFitInst.getMemory());

    worstFitInst.runAll();
    System.out.println("final memory state of worst fit: " + worstFitInst.getMemory());

  }
}
