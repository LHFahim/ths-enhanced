package com.fahim.ths;

import com.fahim.ths.repo.DatabaseInit;


public class BootstrapDB {
    public static void main(String[] args) throws Exception {
        DatabaseInit.init();
        DatabaseInit.seed();
        System.out.println("db initialized ok");
    }
}
