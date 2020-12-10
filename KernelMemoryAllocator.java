package kernelmemoryallocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class KernelMemoryAllocator {

    public static void main(String[] args) {
        boolean flag = true;
        System.out.println("EJEMPLO DE ESTRATEGIA DE ASIGNACION DE MEMORIA BUDDY SYSTEM");
        int initialMemory = 0,
                val = 0;
        Scanner sc2 = new Scanner(System.in);
        System.out.print("\nIngrese la memoria inicial disponible: ");
        initialMemory = sc2.nextInt();
        while (flag) {
            int example;
            Buddy obj = new Buddy(initialMemory);
            System.out.print("Ingrese (1) para asignar o desasignar memoria utilizando el Buddy System o (2) para salir: ");
            Scanner sc = new Scanner(System.in);
            example = sc.nextInt();
            while (example > 2 || example < 1) {
                System.out.println("\nOpcion invalida, intentelo de nuevo.");
                example = sc.nextInt();
            }
            switch (example) {
                case 1:
                    while (true) {
                        int opt = 0;
                        System.out.println("\n(1) Asignar o (2) Desasignar");
                        opt = sc2.nextInt();
                        if (opt == 1) {
                            System.out.print("\nIngrese el size del objeto que desea asignar: ");
                            val = sc.nextInt(); 
                            if (val < 0) {
                                break;
                            }
                            obj.allocate(val);
                        } else if (opt == 2) {
                            System.out.print("\n\nIngrese el size del objeto que desea desasignar: ");
                            val = sc.nextInt(); // Solo permite powers of 2
                            if (val < 0) {
                                break;
                            }
                            obj.deallocate(val);
                        } else {
                            System.out.println("Opcion invalida.");
                        }
                    }
                    /* 
                    Memoria 128 kB
                    Reservar 32 kB
                    List is: {}, {}, {}, {}, {}, { (32, 63) }, { (64, 127) }, {}
                     */
                    break;
                case 2:
                    flag = false;
                default:
            }

        }
    }

    static class Buddy {

        // Limites superiores e inferiores
        class Pair {

            int lb, ub;

            Pair(int a, int b) {
                lb = a;
                ub = b;
            }
        }

        // tamano de la memoria original
        int size;

        // ArrayList que lleva control de los nodos disponibles 
        ArrayList<Pair> arr[];

        // Hashmap que guarda la direccion de inicio y el tamano del segmento a ser asignado.
        // Utiliza la direccion como llave.
        HashMap<Integer, Integer> hm;

        @SuppressWarnings("unchecked")
        Buddy(int s) {

            size = s;
            hm = new HashMap<>();

            // Todos los powers de 2
            int x = (int) Math.ceil(Math.log(s) / Math.log(2));

            arr = new ArrayList[x + 1];

            for (int i = 0; i <= x; i++) {
                arr[i] = new ArrayList<>();
            }

            // Inicialmente solo el bloque mas grande esta disponible.
            arr[x].add(new Pair(0, size - 1));
        }

        void allocate(int s) {

            // Divide entre dos los segmenetos hasta llegar al mas cercano al valor.
            int x = (int) Math.ceil(Math.log(s) / Math.log(2));

            int i;
            Pair temp = null;

            if (arr[x].size() > 0) {

                // Se eliminar de la lista de nodos disponibles ya que sera asignado
                temp = (Pair) arr[x].remove(0);
                System.out.println("Memoria desde " + temp.lb
                        + " hasta " + temp.ub + " asignada");

                // Se guarda en Hashmap
                hm.put(temp.lb, temp.ub - temp.lb + 1);
                return;
            }

            // Si no, busca a un bloque mas grande
            for (i = x + 1; i < arr.length; i++) {

                if (arr[i].isEmpty()) {
                    continue;
                }

                break;
            }

            // Memoria agotada
            if (i == arr.length) {

                System.out.println("Error al momento de asignar memoria.");
                return;
            }

            temp = (Pair) arr[i].remove(0);

            i--;

            // Recorre la lista
            for (; i >= x; i--) {

                // Divide el bloque en mitad y crea dos pares, uno inferior y otro superior
                Pair newPair = new Pair(temp.lb, temp.lb
                        + (temp.ub - temp.lb) / 2);

                Pair newPair2 = new Pair(temp.lb
                        + (temp.ub - temp.lb + 1) / 2,
                        temp.ub);

                arr[i].add(newPair);
                arr[i].add(newPair2);

                temp = (Pair) arr[i].remove(0);
            }

            // Finally inform the user 
            // of the allocated location in memory 
            System.out.println("Memoria desde " + temp.lb
                    + " hasta " + temp.ub + " asignada");

            // Store in HashMap 
            hm.put(temp.lb, temp.ub - temp.lb + 1);
        }

        void deallocate(int s) {
            if (!hm.containsKey(s)) {
                System.out.println("Error al momento de desasignar memoria.");
                return;
            }

            int x = (int) Math.ceil(Math.log(hm.get(s))
                    / Math.log(2));
            int i, buddyNumber, buddyAddress;

            // Se desasigna y se agrega a la lista de nodos disponibles nuevamente 
            arr[x].add(new Pair(s, s + (int) Math.pow(2, x) - 1));
            System.out.println("Memoria desde " + s + " hasta "
                    + (s + (int) Math.pow(2, x) - 1) + " liberada");

            // Calculate it's buddy number and buddyAddress. The 
            // base address is implicitly 0 in this program, so no 
            // subtraction is necessary for calculating buddyNumber 
            buddyNumber = s / hm.get(s);

            if (buddyNumber % 2 != 0) {
                buddyAddress = s - (int) Math.pow(2, x);
            } else {
                buddyAddress = s + (int) Math.pow(2, x);
            }

            // Buscar en la lista de ojetos libres
            for (i = 0; i < arr[x].size(); i++) {
 
                if (arr[x].get(i).lb == buddyAddress) {

                    if (buddyNumber % 2 == 0) {

                        // Add to appropriate free list 
                        arr[x + 1].add(new Pair(s, s
                                + 2 * ((int) Math.pow(2, x)) - 1));
                        System.out.println("Coalescing de bloques "
                                + s + " y "
                                + buddyAddress + " realizado");
                    }
                    else {

                        // Add to appropriate free list 
                        arr[x + 1].add(new Pair(buddyAddress,
                                buddyAddress + 2 * ((int) Math.pow(2, x))
                                - 1));
                        System.out.println("Coalescing de bloques desde "
                                + buddyAddress + " hasta "
                                + s + " realizada");
                    }

                    // Remove the individual segements  
                    // as they have coalesced 
                    arr[x].remove(i);
                    arr[x].remove(arr[x].size() - 1);
                    break;
                }
            }

            // Remove entry from HashMap 
            hm.remove(s);
        }
    }
}
