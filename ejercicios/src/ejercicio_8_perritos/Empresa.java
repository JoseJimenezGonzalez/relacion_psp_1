package ejercicio_8_perritos;

import ejercicio_9_supermercado.Supermercado;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Empresa {

    static int numeroRacionesEnElPlato = 0;
    static int numeroRacionesQueRellena = 4;
    static boolean hanComidoTodosLosPerritos = false;
    static Semaphore semaforoPerritos = new Semaphore(1);
    static Semaphore semaforoPlatoComida = new Semaphore(1);

     public static class Trabajador extends Thread{
         @Override
         public void run() {
             while (!hanComidoTodosLosPerritos){
                 try{
                     semaforoPlatoComida.acquire();
                     rellenarPlatoComida();
                     semaforoPlatoComida.release();
                     Thread.sleep(5000);
                 }catch (InterruptedException ie){
                     ie.printStackTrace();
                 }
             }
         }

         private void rellenarPlatoComida() {
             numeroRacionesEnElPlato = numeroRacionesQueRellena;
             System.out.println("El trabajador está rellenando el plato de comida. Raciones disponibles: " + numeroRacionesEnElPlato);
         }
     }
    public static class Perrito extends Thread{
        Random random = new Random();
        int edad;
        boolean haComido;
        String nombre;
        int racionesNecesarias;
        int edadAleatoriaPerrito = random.nextInt(12);
        Perrito(String nombre){
            this.nombre = nombre;
            haComido = false;
            edad = edadAleatoriaPerrito;
            calcularRacionesNecesarias();
        }

        private void calcularRacionesNecesarias() {
            if (edad == 0) {
                racionesNecesarias = 1;
            } else if (edad >= 1 && edad <= 3) {
                racionesNecesarias = 2;
            } else {
                racionesNecesarias = 3;
            }
        }

        @Override
        public void run() {
                try{
                    while(!haComido){
                        semaforoPerritos.acquire();
                        semaforoPlatoComida.acquire();
                        if(numeroRacionesEnElPlato >= racionesNecesarias){
                            comer();
                            haComido = true;
                            semaforoPlatoComida.release();
                        }else{
                            System.out.println("El cachorro " + nombre + " de meses " + edad + " no ha podido comer porque no hay suficiente comida");
                            semaforoPlatoComida.release();
                            Thread.sleep(2000);
                        }
                        semaforoPerritos.release();
                    }
                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
        }

        private void comer() {
            try {
                System.out.println("El cachorro " + nombre + " de edad " + " meses está comiendo");
                Thread.sleep(edad == 0 ? 4000 : (edad >= 1 && edad <= 3 ? 2000 : 1000));
                numeroRacionesEnElPlato -= racionesNecesarias;
                System.out.println("El cachorro " + nombre + " de edad " + " meses ha comido. Raciones restantes: " + numeroRacionesEnElPlato);
                System.out.println("El cachorro se va a jugar con otros perros");
            }catch (InterruptedException ie){
                ie.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        Trabajador trabajador = new Trabajador();
        trabajador.start();
        Perrito[] perritos = new Perrito[6];
        perritos[0] = new Perrito("Danko");
        perritos[1] = new Perrito("Violeta");
        perritos[2] = new Perrito("Abis");
        perritos[3] = new Perrito("Ufo");
        perritos[4] = new Perrito("Rufo");
        perritos[5] = new Perrito("Scott");
        for (Perrito perrito : perritos){
            perrito.start();
        }
        for (Perrito perrito : perritos) {
            try {
                perrito.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cerrarComedero();
        try{
            trabajador.join();
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }
    }

    private static void cerrarComedero() {
         hanComidoTodosLosPerritos = true;
    }

}
