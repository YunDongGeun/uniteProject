import uniteProject.view.FirstView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {
//    public static void main(String[] args) {
//        FirstView firstView = new FirstView();
//
//        firstView.firstView();
//
//    }

    //소켓기초
    public static void main(String args[]){
        InetAddress ir;
        InetAddress[] irArr;
        String hostname;
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(System.in));

        try{
            System.out.print("Input Host name: ");
            if((hostname = br.readLine()) != null){
                System.out.println();
                ir = InetAddress.getByName(hostname);
                System.out.println("Host name: " + ir.getHostName());
                System.out.println("IP address: " + ir.getHostAddress());
                System.out.println();

                irArr = InetAddress.getAllByName(hostname);
                for(int i =0; i < irArr.length;i++){
                    System.out.println("IP["+i+"]: " + irArr[i]);
                }
                System.out.println();
            }

            InetAddress laddr = InetAddress.getLocalHost();
            System.out.println("Local host name: " + laddr.getHostName());
            System.out.println("Local IP address: " + laddr.getHostAddress());
        } catch(IOException e){
            System.out.println(e);
        }
    }

//    public static void main(String args[]){
//        for(int i=80; i<82; i++){
//            try{
//                System.out.print("Checking Port number " + i +" ==> ");
//                Socket cliSocket = new Socket("www.naver.com", i);
//                System.out.println("in use");
//                cliSocket.close();
//            }catch(UnknownHostException e){
//                System.err.println("Server not found");
//            }catch(SocketException e){
//                System.err.println("not connected");
//            }catch(IOException e){
//                System.err.println(e);
//            }
//        }
//    }
}