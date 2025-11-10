// Implement Pass-II of two pass assembler for pseudo-machine in Java using object oriented features. The output of assignment-1
// (intermediate file and symbol table) should be input for this assignment.

import java.io.*;
import java.util.Scanner;

public class Pass2 {

    static Obj[] symb_table = new Obj[10];
    static Obj[] literal_table = new Obj[10];
    static int symb_found = 0;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("ENTER TOTAL NUMBER OF SYMBOLS: ");
        int total_symb = sc.nextInt();
        for (int i = 0; i < total_symb; i++) {
            symb_table[i] = new Obj("", 0);
            System.out.println("ENTER SYMBOL NAME: ");
            symb_table[i].name = sc.next();
            System.out.println("ENTER SYMBOL ADDRESS: ");
            symb_table[i].addr = sc.nextInt();
        }

        System.out.println("ENTER TOTAL NUMBER OF LITRALS: ");
        int total_ltr = sc.nextInt();
        for (int i = 0; i < total_ltr; i++) {
            literal_table[i] = new Obj("", 0);
            System.out.println("ENTER LITERAL NAME: ");
            literal_table[i].name = sc.next();
            System.out.println("ENTER LITERAL ADDRESS: ");
            literal_table[i].addr = sc.nextInt();
        }

        System.out.println("\n****SYMBOL TABLE****");
        System.out.println("\nSYMBOL\tADDRESS");
        for (int i = 0; i < total_symb; i++) {
            System.out.println(symb_table[i].name + "\t" + symb_table[i].addr);
        }

        System.out.println("\n******LITERAL TABLE******");
        System.out.println("\nIndex\tLITERAL\tADDRESS");
        for (int i = 0; i < total_ltr; i++) {
            System.out.println((i + 1) + "\t" + literal_table[i].name + "\t" + literal_table[i].addr);
        }

        BufferedReader br2 = new BufferedReader(new FileReader("output.txt"));
        String line;
        boolean symbol_error = false, undef_mnemonic = false;
        System.out.println("\n****************OUTPUT FILE****************\n\n");

        lab: while ((line = br2.readLine()) != null)
         {
            String[] token_list = line.split("\\s+",5);
            symbol_error = false;
            undef_mnemonic = false;
            
            lab1: for (String token : token_list) {
                if (token.isEmpty()) {
                    continue; 
                }

                if (token.matches("[0-9]+")) { 
                    System.out.print("\n" + token);
                } else if (token.startsWith("(") && token.endsWith(")")) {
                    String content = token.substring(1, token.length() - 1);
                    
                    String[] parts = content.split(",");
                    
                    if (parts.length == 2) {
                        String letters = parts[0].trim();
                        int num = Integer.parseInt(parts[1].trim());
                        
switch (letters.toUpperCase()) {
    case "S":
        if (num > 0 && num <= total_symb && symb_table[num - 1].addr != 0) {
            System.out.print("\t" + symb_table[num - 1].addr);
        } else {
            System.out.print("\t---");
            symbol_error = true;
        }
        break;
    case "L":
            if (num > 0 && num <= total_ltr)
             {
            System.out.print("\t" + literal_table[num - 1].addr);
             } else {
             System.out.print("\t---");
             symbol_error = true;
        }
        break;
    case "AD":
        System.out.print("\n");
        continue lab;
    case "DL":
        switch (num) {
            case 1:
                System.out.print("\n");
                continue lab;
            case 2:
                System.out.print("\t 00 \t 00");
                continue lab1;
        }
        break;
    case "C":
        System.out.print(String.format("\t%03d", num));
        break;
    default: 
        System.out.print(String.format("\t%03d", num));
        break;
            }
        
            }
        }
    }
}
        
        System.out.println(); 
        
        if (symbol_error) {
            System.out.print("\n\n************SYMBOL IS NOT DEFINED*****");
        }
        if (undef_mnemonic) {
            System.out.print("\n\n****************INVALID MNEMONIC******");
        }

        int[] flag = new int[total_symb];
        for (int i = 0; i < total_symb; i++) {
            symb_found = 0;
            for (int j = 0; j < total_symb; j++) {
                if (symb_table[i].name.equalsIgnoreCase(symb_table[j].name) && flag[j] == 0) {
                    symb_found++;
                    if(symb_found > 1) flag[j] = 1;
                }
            }
            if (symb_found > 1) {
                System.out.print("\n\n*******" + symb_table[i].name + "\" IS DUPLICATE SYMBOL");
            }
        }
        br2.close();
        sc.close();
    }
}

class Obj {
    String name;
    int addr;

    Obj(String nm, int address) {
        this.name = nm;
        this.addr = address;
    }
}