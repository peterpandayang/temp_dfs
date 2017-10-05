package edu.usfca.cs.handler;

/**
 * Created by bingkunyang on 10/4/17.
 */
public class ErrorChecker {

    public ErrorChecker(){

    }

    public void check(String dataCheckSum, String checkCheckSum, String type, String side){
        switch (type){
            case  "network":
                if(dataCheckSum.equals(checkCheckSum)){
                    System.out.println(type + " working well for " + side + " side");
                }
                else{
                    System.out.println(type + " has problem for " + side + " side");
                }
            case "loaddisk":
                if(dataCheckSum.equals(checkCheckSum)){
                    System.out.println(type + " working well for " + side + " side");
                }
                else{
                    System.out.println(type + " has problem for " + side + " side");
                }
        }
    }

}
