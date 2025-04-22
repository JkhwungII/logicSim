import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class Gate {
    private int chkTimes = 0;
    private String Gname, GType;
    private operator OP;
    private boolean[] chkFlag;
    private String[] inputGateNames;
    //private ArrayList<Gate> childGates = new ArrayList<>();
    private ArrayList<Gate> inputGate = new ArrayList<>();
    private int value;


    Gate(String Gname, String GType, String[] inputGateNames){
        this.Gname = Gname;
        this.GType = GType;
        switch (GType) {
            case "and":
                this.OP = new AND();
                break;
            case "or":
                this.OP = new OR();
                break;
            case "xor":
                this.OP = new XOR();
                break;
            case "nor":
                this.OP = new NOR();
                break;
            case "Xnor":
                this.OP = new XNOR();
                break;
            case "nand":
                this.OP = new NAND();
                break;
            case "buf":
                this.OP = new BUF();
                break;
            case "not":
                this.OP = new NOT();
                break;
            default:
                this.OP = null;
                break;
        }
        this.inputGateNames = inputGateNames;
       if(inputGateNames!=null){
           chkFlag = new boolean[inputGateNames.length];
           Arrays.fill(chkFlag,false);
       }
    }


    /*public ArrayList<Gate> getChildGates() {
        return childGates;
    }*/

    public int getValue() {
        return value;
    }

    public String getGname() {
        return Gname;
    }

    public String[] getInputGateNames() {
        return inputGateNames;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /*public void addChildGate(Gate childGate){
        childGates.add(childGate);
    }*/

    public void addInputGates(Gate g){
        inputGate.add(g);
    }

    public boolean isInputGateLevelFound(int index){
        return chkFlag[index];
    }

    public boolean isGateLeveled(){
        return chkTimes == inputGateNames.length;
    }

    public void foundInputGateLevel(int index){
        chkTimes++;
        chkFlag[index] = true;
    }

    public int runGate(int[] inputs){
        return this.OP.runOperation(inputs);
    }

    public void runGate_ux(){
        int[] inputValues = new int[this.inputGate.size()];
        for (int i = 0; i <inputValues.length ; i++) {
            inputValues[i] = this.inputGate.get(i).getValue();
        }
        value = runGate(inputValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gate gate = (Gate) o;
        return Gname.equals(gate.Gname);
    }

    @Override
    public int hashCode() {
        return Gname.hashCode();
    }

}

interface operator{
    int runOperation(int[] inputs);
}

class AND implements operator{
    public int runOperation(@NotNull int[] inputs){
        int x = inputs[0];
        for (int i : inputs)
            x = x&i;
        return x;
    }
}

class OR implements operator{
    public int runOperation(@NotNull int[] inputs){
        int x = inputs[0];
        for (int i : inputs)
            x = x|i;
        return x;
    }
}

class XOR implements operator{
    public int runOperation(@NotNull int[] inputs){
        int x = inputs[0];
        for (int i : inputs)
            x = x^i;
        return x;
    }
}

class NAND extends AND{
    public int runOperation(@NotNull int[] inputs){
        return (super.runOperation(inputs)==0)?1:0 ;
    }
}

class NOR extends OR{
    public int runOperation(@NotNull int[] inputs){
        return (super.runOperation(inputs)==0)?1:0 ;
    }
}

class XNOR extends XOR{
    public int runOperation(@NotNull int[] inputs){
        return (super.runOperation(inputs)==0)?1:0 ;
    }
}

class BUF implements operator{
    public int runOperation(@NotNull int[] inputs){
        return inputs[0];
    }
}

class NOT extends BUF{
    public int runOperation(@NotNull int[] inputs){
        return (super.runOperation(inputs)==0)?1:0 ;
    }
}