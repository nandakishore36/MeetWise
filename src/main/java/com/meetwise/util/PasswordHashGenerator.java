package com.meetwise.util;

import java.util.Scanner;
//import com.meetwise.util.PasswordUtil;

public class PasswordHashGenerator {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.println("MeetWise Password Hash Generator");
        System.out.print("Enter password: ");

        String password = sc.nextLine();

        if (password.length() < 8) {
            System.out.println("Password must contain at least 8 characters.");
            sc.close();
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);

        System.out.println("\nGenerated Password Hash:");
        System.out.println(hashedPassword);

        sc.close();
    }
}
