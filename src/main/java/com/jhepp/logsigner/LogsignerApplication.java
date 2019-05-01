package com.jhepp.logsigner;

import com.jhepp.logsigner.model.LogSignature;
import com.jhepp.logsigner.model.SignatureNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

@SpringBootApplication
public class LogsignerApplication implements CommandLineRunner {

    @Autowired
    ResourceLoader loader;

    private static final String DEBUG_ARG = "-d";
    private static boolean DEBUG_MODE;

    public static void main(String[] args) {
        SpringApplication.run(LogsignerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("EXECUTING : command line runner");

        if (args.length == 0) {
            showUsageAndQuit();
        }

        LogSignature logSignature = new LogSignature();
        List<SignatureNode> firstLevelNodes = new LinkedList<>();
        try (Stream<String> stream = Files.lines(Paths.get(args[0]))) {
            DEBUG_MODE = args.length > 1 && DEBUG_ARG.equalsIgnoreCase(args[1]);
            stream.forEach(line -> {
                        SignatureNode node = new SignatureNode();
                        node.setValue(DigestUtils.sha256Hex(line));
                        firstLevelNodes.add(node);
                        if (DEBUG_MODE) {
                            System.out.println(line + " -> " + node.getValue());
                        }
                    }
            );
        } catch (IOException e) {
            System.out.println(e.getMessage());
            showUsageAndQuit();
        }

        logSignature.buildTree(firstLevelNodes);

        if (DEBUG_MODE) {
            printNewLine();
            logSignature.printTree();
            printNewLine();
        }

        System.out.println(String.format("The Log Signature was successfully built. The file signature is %s.",
                logSignature.getRoot().getValue()));
        System.out.println("Please provide a log entry line to verify (Cmd + C to exit):");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String value = scanner.nextLine();
                String hashChain = logSignature.retrieveHashChain(DigestUtils.sha256Hex(value));
                System.out.println(Strings.isEmpty(hashChain) ? "This log entry does not exist in the log."
                        : "This log entry exists in the log. See the hash chain below:");
                printNewLine();
                System.out.println(hashChain);
                printNewLine();
            }
        }
    }

    private void showUsageAndQuit() {
        System.out.println("\n\nUsage: java -jar logsigner.jar log_filename [-d]\n\n -d   debug mode\n\n");
        System.exit(0);
    }

    private void printNewLine() {
        System.out.println();
    }


}
