package ejercicio_9_supermercado;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Supermercado {
    static Semaphore[] semaforoCajaRegistradora = {new Semaphore(1), new Semaphore(1), new Semaphore(1)};
    static Semaphore semaforoTotalRecaudacion = new Semaphore(1);
    static Semaphore semaforoTotalClientesAtendidos = new Semaphore(1);
    static int totalClientesAtendidos = 0;
    static int[] cantidadRecaudadaCajaRegistradora = new int[3];
    static int cantidadTotalRecaudadaSupermercado = 0;
    static boolean supermercadoAbierto = true;

    private static void cerrarSupermercado(){
        supermercadoAbierto = false;
    }

    public static class Cajero extends  Thread{
        @Override
        public void run(){
            while (supermercadoAbierto){
                try{
                    Thread.sleep(3000);
                    System.out.println("Cajero libre!.");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static class Cliente extends Thread{
        Random random = new Random();
        int numeroAleatorioElegirCaja = random.nextInt(3);
        int cantidadAleatoriaPrecioCompra = random.nextInt(500) + 5;
        String nombre;
        int idCajaPaga;
        String nombreCajaPaga;
        int importeCompra;
        Cliente(String nombre){
            importeCompra = cantidadAleatoriaPrecioCompra;
            idCajaPaga = numeroAleatorioElegirCaja;
            this.nombre = nombre;
            switch (numeroAleatorioElegirCaja){
                case 0:
                    nombreCajaPaga = "Caja 1";
                    break;
                case 1:
                    nombreCajaPaga = "Caja 2";
                    break;
                case 2:
                    nombreCajaPaga = "Caja 3";
                    break;
                default:
                    throw new AssertionError();
            }

        }
        private void hacerCompra(){
            try {
                System.out.println("El cliente " + nombre + " está haciendo la compra.");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        private void pagarCuenta(){
            try{
                //Semaforo para impedir que se metan varios clientes en la misma caja a la vez
                semaforoCajaRegistradora[idCajaPaga].acquire();
                System.out.println("El cliente " + nombre + " esta pagando en la " + nombreCajaPaga + ". Total a pagar: " + importeCompra);
                cantidadRecaudadaCajaRegistradora[idCajaPaga] += importeCompra;
                Thread.sleep(3000);
                //Total de la recaudacion
                semaforoTotalRecaudacion.acquire();
                cantidadTotalRecaudadaSupermercado += importeCompra;
                semaforoTotalRecaudacion.release();
                //Total clientes atendidos
                semaforoTotalClientesAtendidos.acquire();
                totalClientesAtendidos++;
                semaforoTotalClientesAtendidos.release();
                System.out.println(nombre + " se despide despues de pagar.");
                semaforoCajaRegistradora[idCajaPaga].release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            hacerCompra();
            pagarCuenta();
        }
    }

    public static void main(String[] args) {
        //Primero los cajeros de las cajas
        Cajero[] cajeros = new Cajero[3];
        for (int i = 0; i < 3; i++){
            cajeros[i] = new Cajero();
            cajeros[i].start();
        }
        //Clientes
        Cliente[] clientes = new Cliente[10];
        //Nombres
        clientes[0] = new Cliente("Jose");
        clientes[1] = new Cliente("Javi");
        clientes[2] = new Cliente("Roberto");
        clientes[3] = new Cliente("Jose Maria");
        clientes[4] = new Cliente("Alex");
        clientes[5] = new Cliente("Toñi");
        clientes[6] = new Cliente("Sonia");
        clientes[7] = new Cliente("Raquel");
        clientes[8] = new Cliente("Marta");
        clientes[9] = new Cliente("Paula");
        //Start
        for(Cliente cliente : clientes){
            cliente.start();
        }
        //Join
        for (Cliente cliente : clientes) {
            try {
                cliente.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cerrarSupermercado();
        //Cajeros
        for (Cajero cajero : cajeros) {
            try {
                cajero.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Imprimimos las estadisticas
        System.out.println("Cierre del supermercado");
        System.out.println("Total clientes atendidos: " + totalClientesAtendidos);
        System.out.println("Total dinero recaudado: " + cantidadTotalRecaudadaSupermercado);
        System.out.println("Recaudacion por caja");
        System.out.println("Caja 1: " + cantidadRecaudadaCajaRegistradora[0]);
        System.out.println("Caja 2: " + cantidadRecaudadaCajaRegistradora[1]);
        System.out.println("Caja 3: " + cantidadRecaudadaCajaRegistradora[2]);
    }
}
