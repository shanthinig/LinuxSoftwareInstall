/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linuxsoftwareinstall;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LinuxSoftwareInstall {

    private static Session session;
    private static ChannelShell channel;
//    protected static String username = "root";
//    protected static String password = "corent123$$";
//    protected static String hostname = "192.168.0.128";
    protected static String username;
    protected static String password;
    protected static String hostname;

    LinuxSoftwareInstall(String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    private static Session getSession() {
        if (session == null || !session.isConnected()) {
            session = connect(hostname, username, password);
        }
        return session;
    }

    private static Channel getChannel() {
        if (channel == null || !channel.isConnected()) {
            try {
                channel = (ChannelShell) getSession().openChannel("shell");
                channel.connect();

            } catch (Exception e) {
                System.out.println("Error while opening channel: " + e);
            }
        }
        return channel;
    }

    private static Session connect(String hostname, String username, String password) {

        JSch jSch = new JSch();

        try {

            session = jSch.getSession(username, hostname, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);

            System.out.println("Connecting SSH to " + hostname + " - Please wait for few seconds... ");
            session.connect();
            System.out.println("Connected!");
        } catch (Exception e) {
            System.out.println("An error occurred while connecting to " + hostname + ": " + e);
        }

        return session;

    }

    public static void executeCommands(List<String> commands) {

        try {
            Channel channel = getChannel();

            System.out.println("Sending commands...");
            sendCommands(channel, commands);

            readChannelOutput(channel);
            System.out.println("Finished sending commands!");

        } catch (Exception e) {
            System.out.println("An error ocurred during executeCommands: " + e);
        }
    }

    private static void sendCommands(Channel channel, List<String> commands) {

        try {
            PrintStream out = new PrintStream(channel.getOutputStream());

            out.println("#!/bin/bash");
            for (String command : commands) {
                out.println(command);
//                out.write(command.getBytes());

            }
            out.println("exit");

            out.flush();
        } catch (Exception e) {
            System.out.println("Error while sending commands: " + e);
        }

    }

    private static void readChannelOutput(Channel channel) {

        byte[] buffer = new byte[1024];

        try {
            InputStream in = channel.getInputStream();
            String line = "";
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    System.out.println(line);
                }

                if (line.contains("logout")) {
                    break;
                }

                if (channel.isClosed()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
        } catch (Exception e) {
            System.out.println("Error while reading channel output: " + e);
        }

    }

    public static void close() {
        channel.disconnect();
        session.disconnect();
        System.out.println("Disconnected channel and session");
    }

    public static void main(String[] args) {
        List<String> commands = new ArrayList<String>();
//        String docker_install = "yum install docker -y";
//        String cmd = "cat /etc/sudoers";
        String hostname = args[0];
        String username = args[1];
        String password = args[2];
        String cmd = args[3];
        LinuxSoftwareInstall obj = new LinuxSoftwareInstall(hostname, username, password);
//        String cmd = "rpm -qa --queryformat \"%{NAME} %{version}\\n\"";
//        String cmd = "rpm -qa";
        System.out.println("cmd = " + cmd);
//        commands.add("echo '" + password + "' | sudo -S sudo sh -c \"" + cmd + "\"");
        commands.add(cmd);
        executeCommands(commands);
        close();
    }

}
