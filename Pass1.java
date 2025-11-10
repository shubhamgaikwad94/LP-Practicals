// Design suitable data structures and implement pass-I of a two-pass assembler for pseudo-machine in Java using object oriented feature.
// start 100
// movr ax 05
// mover bx 10
// up: add ax bx
// movem a ='5'
// origin up
// ltorg
// movem b ='7'
// ds a 02
// dc b 10
// end

// cd /mnt/c/Users/shubh/Documents
// ls


import java.io.*;
import java.util.*;

class Obj {
    String name;
    int addr;

    Obj(String name, int addr) {
        this.name = name;
        this.addr = addr;
    }
}

public class Pass1 {
    public static void main(String args[]) throws NullPointerException, FileNotFoundException {
        String REG[] = {"ax", "bx", "cx", "dx"};
        String IS[] = {"stop", "add", "sub", "mult", "mover", "movem", "comp", "bc", "div", "read"};
        String DL[] = {"ds", "dc"};

        int temp1 = 0;
        int f = 0;

        Obj[] literal_table = new Obj[10];
        Obj[] symb_table = new Obj[10];
        Obj[] optab = new Obj[60];

        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader("sample.txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));

            boolean start = false;
            boolean end = false;
            boolean fill_addr = false;
            boolean ltorg = false;

            int total_symb = 0, total_ltr = 0, optab_cnt = 0, pooltab_cnt = 0, loc = 0, temp, pos;

            while ((line = br.readLine()) != null && !end) {
                String tokens[] = line.split(" ", 4);

                if (loc != 0 && !ltorg) {
                    if (f == 1) {
                        ltorg = false;
                        loc = loc + temp1 - 1;
                        bw.write("\n" + loc);
                        f = 0;
                        loc++;
                    } else {
                        bw.write("\n" + loc);
                        ltorg = false;
                        loc++;
                    }
                }

                ltorg = fill_addr = false;

                for (int k = 0; k < tokens.length; k++) {
                    pos = -1;

                    // START
                    if (start) {
                        loc = Integer.parseInt(tokens[k]);
                        start = false;
                    }

                    switch (tokens[k]) {
                        case "start":
                            start = true;
                            pos = 1;
                            bw.write("\t(AD," + pos + ")");
                            break;

                        case "end":
                            end = true;
                            pos = 2;
                            bw.write("\t(AD," + pos + ")\n");
                            for (temp = 0; temp < total_ltr; temp++) {
                                if (literal_table[temp].addr == 0) {
                                    literal_table[temp].addr = loc - 1;
                                    bw.write("\t(DL,2)\t(C," + literal_table[temp].name + ")\n" + loc++);
                                }
                            }
                            break;

                        case "origin":
                            pos = 3;
                            bw.write("\t(AD," + pos + ")");
                            pos = search(tokens[++k], symb_table, total_symb);
                            k++;
                            bw.write("\t(C," + symb_table[pos].addr + ")");
                            loc = symb_table[pos].addr;
                            break;

                        case "ltorg":
                            ltorg = true;
                            pos = 5;
                            bw.write("\t(AD," + pos + ")\n");
                            for (temp = 0; temp < total_ltr; temp++) {
                                if (literal_table[temp].addr == 0) {
                                    literal_table[temp].addr = loc - 1;
                                    bw.write("\t(DL,2)\t(C," + literal_table[temp].name + ")\n" + loc++);
                                }
                            }
                            break;

                        case "equ":
                            pos = 4;
                            bw.write("\t(AD," + pos + ")");
                            String prev_token = tokens[k - 1];
                            int pos1 = search(prev_token, symb_table, total_symb);
                            pos = search(tokens[++k], symb_table, total_symb);
                            symb_table[pos1].addr = symb_table[pos].addr;
                            bw.write("\t(S," + (pos + 1) + ")");
                            break;
                    }

                    // IS Check
                    if (pos == -1) {
                        pos = search(tokens[k], IS);
                        if (pos != -1) {
                            bw.write("\t(IS," + pos + ")");
                            optab[optab_cnt++] = new Obj(tokens[k], pos);
                        } else {
                            // DL (DC/DS)
                            pos = search(tokens[k], DL);
                            if (pos != -1) {
                                if (pos == 0) f = 1;
                                bw.write("\t(DL," + (pos + 1) + ")");
                                optab[optab_cnt++] = new Obj(tokens[k], pos);
                                fill_addr = true;
                            } else if (tokens[k].matches("[a-zA-Z]+:")) { // Label
                                pos = search(tokens[k], symb_table, total_symb);
                                if (pos == -1) {
                                    symb_table[total_symb++] = new Obj(tokens[k].substring(0, tokens[k].length() - 1), loc - 1);
                                    bw.write("\t(S," + total_symb + ")");
                                }
                            }
                        }
                    }

                    // REG or LITERAL or CONSTANT
                    if (pos == -1) {
                        pos = search(tokens[k], REG);
                        if (pos != -1) {
                            bw.write("\t(RG," + (pos + 1) + ")");
                        } else if (tokens[k].matches("='(\\d+)'")) {
                            String s = tokens[k].substring(2, 3);
                            literal_table[total_ltr++] = new Obj(s, 0);
                            bw.write("\t(L," + total_ltr + ")");
                        } else if (tokens[k].matches("\\d+")) {
                            bw.write("\t(C," + tokens[k] + ")");
                            temp1 = Integer.parseInt(tokens[k]);
                        } else {
                            pos = search(tokens[k], symb_table, total_symb);
                            if (fill_addr && pos != -1) {
                                symb_table[pos].addr = loc - 1;
                                fill_addr = false;
                            } else if (pos == -1) {
                                symb_table[total_symb++] = new Obj(tokens[k], 0);
                                bw.write("\t(S," + total_symb + ")");
                            } else {
                                bw.write("\t(S," + pos + ")");
                            }
                        }
                    }
                }
            }

            // Display Symbol Table
            System.out.println("\n*SYMBOL TABLE*");
            System.out.println("SYMBOL\tADDRESS");
            for (int i = 0; i < total_symb; i++) {
                System.out.println(symb_table[i].name + "\t" + symb_table[i].addr);
            }

            // Display Literal Table
            System.out.println("\n*LITERAL TABLE*");
            System.out.println("Index\tLITERAL\tADDRESS");
            for (int i = 0; i < total_ltr; i++) {
                if (literal_table[i].addr == 0) {
                    literal_table[i].addr = loc++;
                }
                System.out.println(i + "\t" + literal_table[i].name + "\t" + literal_table[i].addr);
            }

            // Display Opcode Table
            System.out.println("\n*OPTABLE*");
            System.out.println("MNEMONIC\tOPCODE");
            for (int i = 0; i < IS.length; i++) {
                System.out.println(IS[i] + "\t\t" + i);
            }

            br.close();
            bw.close();

        } catch (Exception e) {
            System.out.println("Error while reading the file.");
            e.printStackTrace();
        }

        // Display output.txt
        try {
            BufferedReader br = new BufferedReader(new FileReader("output.txt"));
            System.out.println("\n*OUTPUT FILE CONTENT*\n");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int search(String token, String[] list) {
        for (int i = 0; i < list.length; i++) {
            if (token.equalsIgnoreCase(list[i])) {
                return i;
            }
        }
        return -1;
    }

    public static int search(String token, Obj[] list, int cnt) {
        for (int i = 0; i < cnt; i++) {
            if (token.equalsIgnoreCase(list[i].name)) {
                return i;
            }
        }
        return -1;
    }
}
