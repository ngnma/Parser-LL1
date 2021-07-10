import java.io.*;
import java.util.*;

public class TopDownParser {
    public static String[][] part;
    public static String[][] parseTable;
    public static ArrayList<String> NoneTerminals;
    public static ArrayList<String> Terminals;
    public static HashMap<String,ArrayList<String>> firstMap;
    public static HashMap<String,ArrayList<String>> followMap;
    public static HashMap<Integer,ArrayList<String>> predict;
    public static void main(String[] args) {
        //File file = new File("C:/file.txt");//zahra
        File file = new File("/Users/ngn/Desktop/file.txt");//negin
        run(file);

        //print_first();
        //print_follow();

    }

    public static void set_first(){
        firstMap=new HashMap<>();

        for (int i = 0,j=1; i <part.length ; i++) {
            if (!firstMap.containsKey(part[i][0]))
                firstMap.put(part[i][0], new ArrayList<String>());
            firstMap.get(part[i][0]).add(part[i][j]);
            try {
                while (is_nullable(part[i][j])){
                    j++;
                    firstMap.get(part[i][0]).add(part[i][j]);
                }
            }catch (ArrayIndexOutOfBoundsException e){}

        }


        //replace Nonterminals with their first list
        for (Map.Entry<String, ArrayList<String>> entry : firstMap.entrySet()){
            for (int j = 0; j <entry.getValue().size() ; j++) {
                String nonterminalR=entry.getValue().get(j);
                if(Character.isUpperCase(nonterminalR.charAt(0))){
                    entry.getValue().remove(nonterminalR);
                    entry.getValue().addAll(firstMap.get(nonterminalR));
                    j--;
                }
            }
        }

        //remove duplicated item and # from first ArrayList
        for (Map.Entry<String, ArrayList<String>> entry : firstMap.entrySet()){
            Set<String> set=new HashSet<>(entry.getValue());
            entry.getValue().clear();
            entry.getValue().addAll(set);
            entry.getValue().remove("#");
        }
    }

    public static void set_follow(){
        followMap=new HashMap<>();
        followMap.put("S",new ArrayList<>());
        followMap.get("S").add("$");

        for (int i = 0; i <part.length ; i++) {
            for (int j = 1; j <part[i].length ; j++) {
                if(!followMap.containsKey(part[i][j])){
                    followMap.put(part[i][j],new ArrayList<>());
                }
                try {
                    try {
                        followMap.get(part[i][j]).addAll(firstMap.get(part[i][j+1]));//try{} is for part[i][j+1]=terminal.then it isnt in firstMap
                    }catch (NullPointerException e){
                        followMap.get(part[i][j]).add(part[i][j+1]);
                    }


                    //if the rest of the grammer is Nullable follow(left side) should be added here to
                    boolean is_Nullable=true;
                    for (int k = j+1; k <part[i].length; k++) {
                        if(!is_nullable(part[i][k]))
                            is_Nullable=false;break;
                    }
                    if(is_Nullable)
                        followMap.get(part[i][j]).add(part[i][0]);//becuase of follow left side will change I will add that later


                }catch (ArrayIndexOutOfBoundsException e){//is this need to be add for set_first to?
                    if(!part[i][j].equals(part[i][0]))
                        followMap.get(part[i][j]).add(part[i][0]);
                }
            }
        }

        //remove duplicated item and # from follow ArrayList
        for (Map.Entry<String, ArrayList<String>> entry : followMap.entrySet()){
            for (int j = 0; j <entry.getValue().size() ; j++) {
                String nonterminalR=entry.getValue().get(j);
                if(Character.isUpperCase(nonterminalR.charAt(0))){
                    entry.getValue().remove(nonterminalR);
                    entry.getValue().addAll(followMap.get(nonterminalR));
                    j--;
                }
            }
        }

        //remove duplicated item and # from follow ArrayList
        for (Map.Entry<String, ArrayList<String>> entry : followMap.entrySet()){
            Set<String> set=new HashSet<>(entry.getValue());
            entry.getValue().clear();
            entry.getValue().addAll(set);
            entry.getValue().remove("#");
        }



    }

    public static void read_from_file(File file) {
        Scanner scanner;
        String output = "";
        String tmp;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                tmp = "";
                tmp += scanner.nextLine();
                tmp += "\n";
                output += tmp;
            }

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        String[] grammar=output.split("\n");

        part= new String[grammar.length][];

        for (int i = 0; i < grammar.length; i++)
            part[i]=grammar[i].replace(" :","").split(" ");

        NoneTerminals = new ArrayList<>();
        Terminals = new ArrayList<>();

        //this part is for adding Terminals and NoneTerminals to their lists
        for (int i = 0; i < part.length; i++) {
            if (!NoneTerminals.contains(part[i][0]))
                NoneTerminals.add(part[i][0]);

            for (int j = 0; j < part[i].length; j++) {
                if (!Character.isUpperCase(part[i][j].charAt(0)))
                    if (!Terminals.contains(part[i][j]))
                        Terminals.add(part[i][j]);
            }
        }

        Terminals.add("$");
        Terminals.remove("#");

    }

    public static boolean is_nullable(String word) {

        for (int i = 0; i < part.length; i++) {
            if (part[i][0].equals(word)) {
                if (part[i][1].equals("#")){
                    return true;
                }

                //check all for example for this ( A -> C D F ) check C and D and F recursive
                else {
                    int state = 1; //when 1 means all Nullable and 2 means at least one is not Nullable
                    for (int j = 1; j < part[i].length; j++) {
                        if (!is_nullable(part[i][j])) {
                            state = 2;
                            break;
                        }
                    }
                    if (state == 1)
                        return true;
                }
            }
        }
        return false;
    }

    public static void predict() {
        predict = new HashMap<>();

        for (int i = 0; i <part.length ; i++) {
            predict.put(i,new ArrayList<>());
        }

        for (int i = 0; i < part.length; i++) {

            //means a Terminal
            if (!Character.isUpperCase(part[i][1].charAt(0))){
                if (!predict.get(i).contains(part[i][1]))
                    predict.get(i).add(part[i][1]);
            }

            //means being nullable and we should add follow set of A for this(A -> #)
            if (part[i][1].equals("#")) {
                for (int j = 0; j < followMap.get(part[i][0]).size(); j++)
                    if (!predict.get(i).contains(followMap.get(part[i][0]).get(j)))
                        predict.get(i).add(followMap.get(part[i][0]).get(j));
            }

            //for a Terminal we should add first set to predict ( for  1)A - > B D  first of B )
            if (firstMap.get(part[i][1]) != null)
                for (int j = 0; j < firstMap.get(part[i][1]).size(); j++)
                    if (!predict.get(i).contains(firstMap.get(part[i][1]).get(j)))
                        predict.get(i).add(firstMap.get(part[i][1]).get(j));

            //for 1) A - > B D  if B is nullable we should also add follow set of B to predict 1
            if (is_nullable(part[i][1]))
                for (int j = 0; j < followMap.get(part[i][1]).size(); j++)
                    if (!predict.get(i).contains(followMap.get(part[i][1]).get(j)))
                        predict.get(i).add(followMap.get(part[i][1]).get(j));

        }
    }

    public static void make_parse_table() {
        parseTable = new String[NoneTerminals.size()+1][Terminals.size()+1];
        parseTable[0][0] = " ";

        //column of NoneTerminals
        for (int i = 1; i < NoneTerminals.size()+1; i++) {
            parseTable[i][0] = NoneTerminals.get(i-1);
        }

        //Row of Terminals
        for (int i = 1; i < Terminals.size()+1; i++) {
            parseTable[0][i] = Terminals.get(i-1);
        }

        //search which Terminals are in the predict set of K's rule
        for (int k = 0; k < predict.size(); k++) {
            for (int i = 1; i < NoneTerminals.size() + 1; i++) {
                if (part[k][0].equals(parseTable[i][0])) {
                    for (int j = 1; j < Terminals.size() + 1; j++) {
                        if (predict.get(k).contains(parseTable[0][j])) {
                            parseTable[i][j] = "[" + k + "]";
                        }
                    }
                }
            }
        }

        //empty blocks
        for (int i = 0; i < parseTable.length; i++) {
            for (int j = 0; j < parseTable[i].length; j++) {
                if (parseTable[i][j] == null) {
                    parseTable[i][j] = "[]";
                }
            }
        }

    }

    public static void print_first(){
        System.out.println("first");
        for (Map.Entry<String, ArrayList<String>> entry : firstMap.entrySet()){
            System.out.print(entry.getKey() + " : ");
            for (int j = 0; j < entry.getValue().size()-1; j++) {
                System.out.print(entry.getValue().get(j) + ",");
            }
            System.out.println(entry.getValue().get(entry.getValue().size()-1));
        }
    }

    public static void print_follow(){
        System.out.println("follow");
        for (Map.Entry<String, ArrayList<String>> entry : followMap.entrySet()){
            if(Character.isUpperCase(entry.getKey().charAt(0))) {
                System.out.print(entry.getKey() + " : ");
                for (int j = 0; j < entry.getValue().size()-1; j++) {
                    System.out.print(entry.getValue().get(j) + ",");
                }
                System.out.println(entry.getValue().get(entry.getValue().size()-1));
            }
        }
    }

    public static void printParseTable() {
        for (int i = 0; i < parseTable.length; i++) {
            for (int j = 0; j < parseTable[i].length; j++) {
                String s = String.format("%7s", parseTable[i][j]);
                System.out.print(s +" ");
            }
            System.out.println();
        }
    }

    public static void run(File file) {
        read_from_file(file);
        set_first();
        set_follow();
        predict();
        make_parse_table();
        printParseTable();
    }

}