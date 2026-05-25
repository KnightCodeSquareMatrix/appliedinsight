package com.knightcode.appliedstoragesorter.rule.filter;

import java.nio.file.Path;

public final class FilterUiMetadataCli {
    private FilterUiMetadataCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0]) {
            case "write-default" -> writeDefault(args);
            case "inspect" -> inspect(args);
            default -> {
                System.err.println("Unknown command: " + args[0]);
                printUsage();
                System.exit(1);
            }
        }
    }

    private static void writeDefault(String[] args) throws Exception {
        Path outputFile = args.length >= 2
                ? Path.of(args[1])
                : Path.of("tmp/filter-ui-metadata.json");

        FilterUiMetadataJsonCodec.write(FilterUiMetadata.createDefault(), outputFile);
        System.out.println("Wrote default filter UI metadata: " + outputFile);
    }

    private static void inspect(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("inspect requires <metadata.json>");
            printUsage();
            System.exit(1);
        }

        Path inputFile = Path.of(args[1]);
        FilterUiMetadata metadata = FilterUiMetadataJsonCodec.load(inputFile);

        System.out.println("schemaVersion=" + metadata.schemaVersion());
        System.out.println("fields=" + metadata.fields().size());
        System.out.println("operators=" + metadata.operators().size());
        System.out.println("combinators=" + metadata.combinators().size());
    }

    private static void printUsage() {
        System.out.println("Usage: FilterUiMetadataCli <write-default [output.json] | inspect metadata.json>");
    }
}
