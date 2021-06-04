package examples;

import java.security.Provider;

import java.security.Security;

import java.util.Enumeration;



public class CryptoProviderList {

    public static void main(String[] args) throws Exception {

        try {

            Provider[] p = Security.getProviders();

            for (Provider provider : p) {

                System.out.println(provider);

                for (Enumeration<?> e = provider.keys(); e.hasMoreElements(); )

                    System.out.println("\t" + e.nextElement());

            }

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }

    }

}
