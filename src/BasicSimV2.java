import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

public class BasicSimV2 {
     ArrayList<ArrayList<Gate>> sortedGateArray = new ArrayList<>();
     ArrayList<Gate> allInputGateList = new ArrayList<>();
     ArrayList<String> allOutputGateList = new ArrayList<>();
     ArrayList<Gate> unsortedGateList = new ArrayList<>();
     ArrayList<String> inputSignal = new ArrayList<>();
     HashMap<String, Integer> gatesValueMap  = new HashMap<>();
     HashMap<String, Gate> gateLookupMap = new HashMap<>();
     ArrayList<String> outputSignal = new ArrayList<>();
     //int[] currentInput;

    public void BasicSim(String baseDir, String CircType, ArrayList<String> inputSignal, boolean isUX, String Size) throws Exception {
        String benchFile;

        if (isUX){
            benchFile = baseDir + "/" + CircType + "_UX.bench.txt" ;
        }
        else {
            benchFile = baseDir + "/" + CircType + ".bench.txt" ;
        }
        this.inputSignal = inputSignal;
        parseBenchFile(benchFile) ; // turn data sheet into usable form
        if (isUX||Size.equals("1m")||Size.equals("10m")){ // 混亂化 or 模擬數量>1m
            DoLevel(); // organize the gates base on the order they are run
            simulation_ux();
        }
        else {
            simulation();
        }
    }

    public void simulation() {
         for (int i = 0; i < inputSignal.size(); i++) {
             fillInput(inputSignal.get(i));
             for (Gate gate : unsortedGateList) {
                 doSim(gate);
             }
             gatherOutput(i);
         }
     }

    public void simulation_ux(){
        for (int i = 0; i < inputSignal.size(); i++) {
            fillInput_ux(inputSignal.get(i));
            for (int j = 1 ; j < sortedGateArray.size(); j++) {
                ArrayList<Gate> currentLvGates = sortedGateArray.get(j); // run gates one by one though sorted gates
                for (Gate g : currentLvGates) {
                    g.runGate_ux();
                }
            }
            gatherOutput_ux(i);
        }
    }
    //archived due to bad performance
    /*public void simulation_Reactive() {
        int[] lastInput;
        ArrayList<Set<Gate>> toSimLists = new ArrayList<>();
        for (int i = 0; i <SGA.size(); i++) {
            toSimLists.add(new HashSet<Gate>());
        }
        fillInput(inputSignal.get(0));
        for (int j = 1 ; j < SGA.size(); j++) {
            ArrayList<Gate> currentLvGates = SGA.get(j);
            for (Gate currentLvGate : currentLvGates) {
                doSim(currentLvGate);
            }
        }
        gatherOutput(0);
        for (int i = 1; i < inputSignal.size(); i++){
            lastInput = currentInput;
            fillInput(inputSignal.get(i));
            for (int j = 0; j < currentInput.length; j++) {
                if ((currentInput[j] ^ lastInput[j]) == 1) {
                    ArrayList<Gate> childGates = allInputGateList.get(j).getChildGates();
                    for (Gate g : childGates){
                        toSimLists.get(g.getGlevel()).addAll(childGates);
                    }
                }
            }
            for (Set<Gate>s:toSimLists) {
                ArrayList<Gate> doneSim = new ArrayList<>();
                List<Gate> l = new ArrayList<Gate>(s);
                for (Gate g : l){
                    doSim(g);
                    doneSim.add(g);
                }
                for (Gate g :doneSim) {
                    ArrayList<Gate> childGates = g.getChildGates();
                    for (Gate g1: childGates) {
                        toSimLists.get(g1.getGlevel()).add(g1);
                    }
                    s.remove(g);
                }
                doneSim.clear();
            }
            gatherOutput(i);
        }
    }*/

    //fill inputs to a map of (gate names, value)
    public void fillInput(String ipvs) {
        if (ipvs.length() != allInputGateList.size()) {
            throw new java.lang.RuntimeException("Input Size mismatch:"+ipvs.length()+","+allInputGateList.size()) ;
        }
        //currentInput = new int [ipvs.length()];
        for (int i = 0 ; i<ipvs.length(); i++) {
            if (ipvs.charAt(i)=='0') {
                gatesValueMap.put(allInputGateList.get(i).getGname(), 0);
                //currentInput[i] = 0;
            }
            else {
                gatesValueMap.put(allInputGateList.get(i).getGname(), 1);
               //currentInput[i] = 1;
            }
        }
    }
    //fill inputs directly to gate
    public void fillInput_ux(String ipvs) {
        if (ipvs.length() != allInputGateList.size()) {
            throw new java.lang.RuntimeException("Input Size mismatch:"+ipvs.length()+","+allInputGateList.size()) ;
        }
        for (int i = 0 ; i<ipvs.length(); i++) {
            if (ipvs.charAt(i)=='0') {
                allInputGateList.get(i).setValue(0);
            }
            else {
                allInputGateList.get(i).setValue(1);
            }
        }
    }

    public void doSim(Gate g1) {
        String gName = g1.getGname() ;
        String[] inputGateNames = g1.getInputGateNames();
        int[] inputs = new int[inputGateNames.length];
        for (int i = 0; i < inputGateNames.length; i++) {
            inputs[i] = gatesValueMap.get(inputGateNames[i]);
        }
        int v = g1.runGate(inputs);
        gatesValueMap.put(gName, v) ;
    }

    //gather output from a map of (gate names, value)
    public void gatherOutput(int index) {
        String opvs = inputSignal.get(index) + " ";
        for (int i = 0 ; i<allOutputGateList.size(); i++) {
            opvs = opvs.concat(gatesValueMap.get(allOutputGateList.get(i)).toString());
        }
        outputSignal.add(opvs);
    }
    //gather output from output gates
    public void gatherOutput_ux(int index) {
        String opvs = inputSignal.get(index) + " ";
        for (int i = 0 ; i<allOutputGateList.size(); i++) {
            opvs = opvs.concat(Integer.toString((gateLookupMap.get(allOutputGateList.get(i)).getValue())));
        }
        outputSignal.add(opvs);
    }

    public void parseBenchFile(String benchFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(benchFile));
        Pattern gateTrimPattern1 = Pattern.compile("[=(]");
        Pattern gateTrimPattern2 = Pattern.compile("[\\s)]");
        String aLine = "", gName = "";
        LinkedHashSet<Gate> nonInputGate = new LinkedHashSet<>();
        while ((aLine=br.readLine())!=null) {
            if (aLine.startsWith("#")|| aLine.trim().length()==0 )
                continue;
            else if (aLine.startsWith("IN")) {
                gName = aLine.substring(6,aLine.length()-1) ;
                Gate g1 = new Gate(gName,"input",null);
                allInputGateList.add(g1);
                gateLookupMap.put(gName, g1);
            }
            else if (aLine.startsWith("OUT")) {
                gName = aLine.substring(7,aLine.length()-1) ;
                allOutputGateList.add(gName);
            }
            else {
                aLine = gateTrimPattern1.matcher(aLine).replaceAll(",");
                aLine = gateTrimPattern2.matcher(aLine).replaceAll("");
                String[] gateInfo = aLine.split(",") ;
                Gate g1 = new Gate(gateInfo[0],gateInfo[1],Arrays.copyOfRange(gateInfo,2, gateInfo.length));
                nonInputGate.add(g1);
            }
        }
        unsortedGateList.addAll(nonInputGate);
        br.close() ;
        sortedGateArray.add(allInputGateList);
    }

    public void DoLevel () {
        int leveledCnt = 0, currentLV = 1;
        ArrayList<Integer> removeIndex = new ArrayList<>();
        int unsortedAmount = unsortedGateList.size();
        while (leveledCnt < unsortedAmount) {
            sortedGateArray.add(new ArrayList<Gate>());
            /*
            Loop all unsorted gates through the last leveled level to check if its input gates can be found within.
            And when found record the result in the unsorted gate.
            During the process, if a unsorted gate is leveled(found all of its input gate's level),
            level will be fill into the gate, its index will be recorded for removal from unsorted gate list
            and leveled gate count will be increase by 1.
            */
            for (int i = 0; i < unsortedGateList.size(); i++) {
                Gate g1 = unsortedGateList.get(i);
                String[] InputGateNames = g1.getInputGateNames();
                ArrayList<Gate> lowerLevelGates = sortedGateArray.get(currentLV - 1);
                for (int j = 0; j < InputGateNames.length; j++) {
                    if (!g1.isInputGateLevelFound(j)) {
                        String InputGateName = InputGateNames[j];
                        for (Gate k : lowerLevelGates) {
                            String lowerLevelGateName = k.getGname();
                            if (InputGateName.equals(lowerLevelGateName)) {
                                //k.addChildGate(g1);
                                g1.addInputGates(k);
                                g1.foundInputGateLevel(j);
                                if (g1.isGateLeveled()){
                                    sortedGateArray.get(currentLV).add(g1);
                                    removeIndex.add(i);
                                    leveledCnt++;
                                    gateLookupMap.put(g1.getGname(), g1);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            currentLV++;
            Collections.reverse(removeIndex);
            for(int i : removeIndex)
                unsortedGateList.remove(i);
            removeIndex.clear();
        }
    }

    public ArrayList<String> createResult() {
        return outputSignal;
    }
}
