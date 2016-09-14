package net.neoremind.fountain.test.cmd;

import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.neoremind.fountain.producer.SingleProducer;

/**
 * @author zhangxu
 */
public class FountainApplicationLauncher {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                    "Argument should be application context xml. For example, beidouaddb/fountain-config.xml");
            System.exit(-1);
        }
        ApplicationContext context = new ClassPathXmlApplicationContext(args[0]);
        Scanner in = new Scanner(System.in);
        System.out.println("Enter 'stop' to exit.");
        while (in.hasNext()) {
            String input = in.nextLine();
            if (input.equals("stop")) {
                try {
                    Map<String, SingleProducer> producer = context.getBeansOfType(SingleProducer.class);
                    for (SingleProducer singleProducer : producer.values()) {
                        singleProducer.destroy();
                    }
                } catch (BeansException e) {
                    System.err.println("Destroy producer failed");
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }
    }

}
