package edu.virginia.sde.hw1;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import static java.util.Collections.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {
    public static int totalPopulation(List<Integer> Statespop) {
        int totalpop = 0;
        for (int i = 0; i < Statespop.size(); i++) {
            totalpop += Statespop.get(i);
        }
        return totalpop;
        //gets the sum of all the states
    }

    public static int getDivisor(int totalPop, int numberofRep) {
        return totalPop / numberofRep;
        //gets the total population of all states then divides by number of reps; useful for next part (denominator)
    }

    public static double[] DivideColumn(List<Integer> statePop, int averagePopPerRep) {
        double[] statePopAsNumerator = new double[statePop.size()];
        List<Double> StatePop = statePop.stream().map(Integer::doubleValue).toList();
        for (int i = 0; i < statePop.size(); i++) {
            statePopAsNumerator[i] = StatePop.get(i) / averagePopPerRep;
        }
        return statePopAsNumerator;
        //divide column in the writeup, very similar to floor function but returning a double
    }

    public static List<Integer> floor(List<Integer> statePop, double totalPop) {
        List<Integer> minNumofRep = new ArrayList<>(); //allocating new int that gets minimum number of representatives
        if (totalPop == 0) {
            throw new IllegalArgumentException("totalPop cannot be zero.");
        }
        for (int i = 0; i < statePop.size(); i++) { //looping through csv file
            minNumofRep.add((int) (statePop.get(i) / totalPop)); //getting the specific number of rep per state
        }
        return minNumofRep;
    }

    public static List<Double> result(double[] divide, List<Integer> floor) {
        List<Double> remainder = new ArrayList<>();
        for (int i = 0; i < divide.length; i++) {
            remainder.add(i, (divide[i] - floor.get(i)));
        }
        return remainder;
        //this is the remainder for the divide and floor; similar to last two functions
    }

    public static List<Double> quicksort(List<Double> array, int lowIndex, int highIndex) {
        if (lowIndex >= highIndex) {
            return array;
        }

        double pivot = array.get(highIndex);
        int leftPointer = lowIndex;
        int rightPointer = highIndex;

        while (leftPointer < rightPointer) {
            while (array.get(leftPointer) <= pivot && leftPointer < rightPointer) {
                leftPointer++;
            }
            while (array.get(rightPointer) >= pivot && leftPointer < rightPointer) {
                rightPointer--;
            }
            swap(array, leftPointer, rightPointer);
        }

        swap(array, leftPointer, highIndex);
        quicksort(array, lowIndex, leftPointer - 1);
        quicksort(array, leftPointer + 1, highIndex);
        return array;
    }

    //swapping method
    private static void swap(List<Double> array, int index1, int index2) {
        double temp = array.get(index1);
        array.set(index1, array.get(index2));
        array.set(index2, temp);
    }

    // Finding the index of the states that can receive extra seats.
    public static int[] stateIndex_to_assign_extra_sits(double[] remainders, List<Double> sorted_remainders, int repLeft) {
        // Make sure we do not try to assign more seats than there are sorted remainders
        int seatsToAssign = Math.min(repLeft, sorted_remainders.size());
        double[] new_sorted_exrep = new double[seatsToAssign];
        double[] target_remain = new double[seatsToAssign];

        // Make sure we only assign as many seats as we can (limited by seatsToAssign)
        int seatassigned = 0;
        for (int j = sorted_remainders.size() - 1; j >= 0 && seatassigned < seatsToAssign; j--) {
            new_sorted_exrep[seatassigned] = sorted_remainders.get(j);
            target_remain[seatassigned] = sorted_remainders.get(j);
            seatassigned++;
        }

        // Array to tack which states have been assigned an extra seat
        boolean[] assigned = new boolean[remainders.length];

        // Find the index of the states in the unsorted remainders array
        // which correspond to the highest remainders ( thus should receive the extra seats)
        int[] target_state_index = new int[seatsToAssign];
        int matched = 0;

        outerLoop:
        for (double target : target_remain) {
            for (int j = 0; j < remainders.length; j++) {
                if (target == remainders[j] && !assigned[j]) {
                    target_state_index[matched++] = j;
                    assigned[j] = true; // Take note of this state as having been assigned an extra seat

                    // Continue with the next target remainder once a match is found
                    if (matched == seatsToAssign) {
                        break outerLoop;
                    }
                    break;
                }
            }
        }

        // find the index of the two repleft amount of remains in unsorted remainders (which is also the index for the state name)
        int[] states_target_index = new int[repLeft];
        int same = 0;
        for (int i = 0; i < target_remain.length; i++) {
            for (int j = 0; j < remainders.length; j++) {
                if (target_remain[i] == remainders[j]) {
                    states_target_index[same] = j;
                    same++;
                }
            }
        }
        return states_target_index;
    }

    // adding the extra representative to the target states
    public static List<Integer> add_extra_rep(int[] state_index, List<Integer> origin_state_rep, int repleft) {
        int iterations = Math.min(repleft, state_index.length);

        for (int i = 0; i < iterations; i++) {
            if (state_index[i] < origin_state_rep.size()) {
                int currentRep = origin_state_rep.get(state_index[i]);
                origin_state_rep.set(state_index[i], currentRep + 1);
            }
        }
        return origin_state_rep;
    }


    public static void assignTheFirstSeat(List<String> state, Map<String, Integer> rep) {
        for (String s : state) {
            rep.put(s, 1);
        }
    }

    public static void calcPriority(List<String> state, List<Integer> pop, Map<String, Double> priority, Map<String, Integer> rep) {
        for (int i = 0; i < state.size(); i++) {
            priority.put(state.get(i), pop.get(i) / (Math.sqrt(rep.get(state.get(i)) * (rep.get(state.get(i)) + 1))));
        }
    }

    public static String getHighestPriority(Map<String, Double> priority, List<String> state) {
        String highest_key = state.get(0);
        double temp_priority = 0;
        double highest_priority = 0;
        for (String s : state) {
            temp_priority = priority.get(s);
            if (temp_priority > highest_priority) {
                highest_priority = temp_priority;
                highest_key = s;

            }
        }
        return highest_key;
    }

    public static Map<String, Integer> huntingtonHillMethod(Map<String, Integer> rep, List<String> state, List<Integer> pop, Map<String, Double> Priority, int rep_num) {
        assignTheFirstSeat(state, rep);
        //calcPriority(state, pop, Priority, rep);
        //Map<String,Double> Unsorted_Priority = new HashMap<>();
//            sortingPriority(Priority);

        System.out.println("-----");

        int round = 0;
        int count = 0;
        rep_num -= state.size();
        for (int i = 0; i < rep_num; i++) {
//                for (int j = 0; j < Unsorted_Priority.size(); j++) {
//                    if (Objects.equals(Priority.get(0), Unsorted_Priority.get(j))) {
//                        rep.set(j, rep.get(j) + 1);
//                        calcPriority(state, pop, Priority, rep);
//                        sortingPriority(Priority);
//                        count +=1;
//                    }
//
//                }
//                round += 1;
            calcPriority(state, pop, Priority, rep);
            String high_priority = getHighestPriority(Priority, state);
            rep.put(high_priority, rep.get(high_priority) + 1);
        }
        //System.out.println(count);
        //System.out.println(round);
        return rep;
    }//Helped by TA Thomas Nguyen;

    public static void main(String[] args) {
        // CSV file reading
        int totalReps = 435;


        if (args.length == 0) {
            System.out.println("Error: Please Provide a CSV File");
            return;
        }
        boolean HamiltonAlgorithm = false;
        boolean checkNum = false;
        for (int i = 0; i < args.length; i++) {
            System.out.println(i);
            if (args[i].equalsIgnoreCase("--hamilton")) {
                HamiltonAlgorithm = true;
                System.out.println(args[i]);
//                System.out.println(HamiltonAlgorithm);


//                if (i > 0 && !args[i-1].equalsIgnoreCase("--hamilton")) {
//                    if (i > 0 && args[i - 1].equalsIgnoreCase("--hamilton")) {
//                        try {
//                            System.out.println(HamiltonAlgorithm);
//                            totalReps = Integer.parseInt(args[i - 1]);
//                            i++;
//                        } catch (NumberFormatException e) {
//                            System.out.println(args[i]);
//                            System.out.println("Error, Invalid Number of Representatives!");
//                            return;
//                        }
//                    }
//                }


//                else if (i > 0 && args[i-1].equalsIgnoreCase("--hamilton")) {
//                        try {
//                            totalReps = Integer.parseInt(args[i]);
//
//                    } catch  (NumberFormatException e) {
//                        System.out.println(args[i]);
//                        System.out.println("Error, Invalid Number of Representatives%");
//                        return;
//
//                    }

            } else if (i < args.length - 1 && !args[i + 1].equalsIgnoreCase("--hamilton")) {
                try {
                    totalReps = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("Error, Invalid Number of Representatives");
                    return;
                }
            }
            else if (HamiltonAlgorithm && args[i].equalsIgnoreCase("--hamilton")) {
                totalReps = 435;
            }

        }

        System.out.println(HamiltonAlgorithm);
//            for (int i = 0; i < args.length; i++) {
//                if (HamiltonAlgorithm && args.length > 1) {
//                    totalReps = Integer.parseInt(args[1]);
//
//                }
//            }


//            if (HamiltonAlgorithm && args[1].equals("--hamilton")) {
//                System.out.println("hi");
//        }

        String file = args[0];
        String line = "";
        List<String> states = new ArrayList<>();
        List<Integer> populations = new ArrayList<>();

        // Excel file reading
        if (file.endsWith(".xlsx")) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fis);
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                int stateColumn = -1, populationColumn = -1;
                for (Cell cell : headerRow) {
                    String headerValue = cell.getStringCellValue().trim().toLowerCase();
                    if ("state".equals(headerValue)) {
                        stateColumn = cell.getColumnIndex();
                    } else if ("population".equals(headerValue)) {
                        populationColumn = cell.getColumnIndex();
                    }
                }

                if (stateColumn == -1 || populationColumn == -1) {
                    throw new IllegalArgumentException("The file must contain 'State' and 'Population' columns.");
                }

                for (Row row : sheet) {
                    if(row.getPhysicalNumberOfCells() != 0) {
                        if (row.getRowNum() == 0) {
                            continue;
                        }

                        Cell stateCell = row.getCell(stateColumn);
                        Cell populationCell = row.getCell(populationColumn);

                        if (stateCell == null || populationCell == null || populationCell.getCellType() != CellType.NUMERIC) {
                            System.out.println("Warning: Skipping Bad Row");
                            continue;
                        }

                        String state = stateCell.getStringCellValue().trim();
                        int population = (int) populationCell.getNumericCellValue();

                        if (population < 0) {
                            System.out.println("Warning: Skipping Bad Row (Population is Negative)");
                            continue;
                        }

                        states.add(state);
                        populations.add(population);
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error: Excel File Cannot be Found");
                return;
            } catch (IOException e) {
                System.out.println("Error: Unable to Read File");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
            if (states.isEmpty() || populations.isEmpty()) {
                System.out.println("Error: No Valid States with Valid Populations Found in File");
                return;
            }
        }
        // CSV File Reading
        if (file.endsWith(".csv")) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                br.readLine();
                while ((line = br.readLine()) != null) {
                    if (!line.contains(",")){
                        System.out.println("Warning: Skipping Bad Row (Missing Comma)");
                        continue;
                    }
                    String[] values = line.split(",");
                    String state = values[0].trim();
                    int statePop;
                    try {
                        if (values.length < 2) {
                            System.out.println("Warning: Skipping Bad Row (Population is Missing)");
                            continue;
                        }
                        else {
                            statePop = Integer.parseInt(values[1].trim());
                        }
                    } catch (NumberFormatException e){
                        System.out.println("Warning: Skipping Bad Row (Population is Non-Integer)");
                        continue;
                    }
                    if (statePop < 0) {
                        System.out.println("Warning: Skipping Bad Row (Population is Negative)");
                        continue;
                    }
                    states.add(state);
                    populations.add(statePop);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error: CVS File Cannot be Found");
                return;
            }   catch (IOException e) {
                System.out.println("Error: Unable to Read File");
                return;
            }
            if (states.isEmpty() || populations.isEmpty()) {
                System.out.println("Error: No Valid States with Valid Populations Found in File");
                return;
            }
        }

        if (HamiltonAlgorithm) {


            int totalStatePop = totalPopulation(populations);
            System.out.println(totalStatePop);
            int averageNum = getDivisor(totalStatePop, totalReps);
            System.out.println(averageNum);
            List<Integer> representativesPerState = floor(populations, averageNum);
            System.out.println(representativesPerState);
            double[] temp = DivideColumn(populations, averageNum);
            System.out.println(temp);
            List<Double> abc = result(temp, representativesPerState);
            System.out.println(abc);

            int temp_total_rep_assigned = representativesPerState.stream().mapToInt(Integer::intValue).sum();
            System.out.println(temp_total_rep_assigned);

            int rep_sits_left = totalReps - temp_total_rep_assigned;
            System.out.println(rep_sits_left);

            double[] unsorted_abc = new double[abc.size()];
            for (int i = 0; i < abc.size(); i++) {
                unsorted_abc[i] = abc.get(i);
            }

            List<Double> sorted_abc = quicksort(abc, 0, abc.size() - 1);

            int[] states_re_extra = stateIndex_to_assign_extra_sits(unsorted_abc, sorted_abc, rep_sits_left);

            List<Integer> updated_rep_per_state = add_extra_rep(states_re_extra, representativesPerState, rep_sits_left);

            // Return states and reps per state in alphabetical order
            int size = states.size();

            List<Integer> stateIndex = new ArrayList<>();
            for (int i = 0; i < states.size(); i++) {
                stateIndex.add(i);
            }

            sort(stateIndex, (i1, i2) -> states.get(i1).compareTo(states.get(i2)));


            for (int i = 0; i < stateIndex.size(); i++) {
                Integer currentIndex = stateIndex.get(i);
                System.out.printf("%-15s%-2s%-1s%n", states.get(currentIndex), "-", updated_rep_per_state.get(currentIndex));
            }
        } else {
//        List<Integer> rep = new ArrayList<>(Collections.nCopies(states.size(), 0));
            Map<String, Integer> rep = new HashMap<>();
            Map<String, Double> Priority = new HashMap<>();
            huntingtonHillMethod(rep, states, populations, Priority, totalReps);

            List<Integer> stateIndex = new ArrayList<>();
            for (int i = 0; i < states.size(); i++) {
                stateIndex.add(i);
            }

            sort(stateIndex, (i1, i2) -> states.get(i1).compareTo(states.get(i2)));


            for (int i = 0; i < stateIndex.size(); i++) {
                Integer currentIndex = stateIndex.get(i);
                System.out.printf("%-15s%-2s%-1s%n", states.get(currentIndex), "-", rep.get(states.get(currentIndex)));
            }
        }
    }
}