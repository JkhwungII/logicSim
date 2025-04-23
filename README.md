# logicSim
a simulator that simulates logic circuit's output
## How does it work
It parses a txt file that discribes the circuit, and simulates the result the given circuit to create according to the input given. (in form of txt file also)

a example of circuit txt file(naming scheme: \<name\>.(bench/UX).txt): 
  
![image](https://github.com/user-attachments/assets/063d4be5-587a-4ebb-aa2b-272224289ba9)

  
a example of input txt file(naming scheme: \<name\>_\<input size\>_ip.txt):
  
![image](https://github.com/user-attachments/assets/cc67d103-9f1f-49e3-a86f-4a285af0851f)
  
## How to use it
Run logicSim\src\BasicSimV2_Main.java

To cofigure the simulator, edit these lines in the aforementioned file

![image](https://github.com/user-attachments/assets/54fc815e-a783-446c-a299-27483d686ef2)
  
* circType option choose what circuit to test (placed in /Data)
* testSize option choose the size of the input file (need to have according input txt file in /Data)
* isUX option on meant allowing logic gates to be arrage out of the order which its executed and duplication of gates in the circuit txt file. (Does not include output and input gates.)
