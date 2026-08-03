package com.knightcode.appliedstoragesorter.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.knightcode.appliedstoragesorter.profilegen.HeuristicRoutingProfileGenerator;
import com.knightcode.appliedstoragesorter.profilegen.ProfileGenerationRequest;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfileJsonCodec;

public final class SorterDumpProfileGenerationAnalyzer {
    private SorterDumpProfileGenerationAnalyzer() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || args.length > 4) {
            System.err.println(
                    "Usage: SorterDumpProfileGenerationAnalyzer <base-profile.json> <dump.json> <output-profile.json> [notes.txt]");
            System.exit(1);
        }

        Path baseProfileFile = Path.of(args[0]);
        Path dumpFile = Path.of(args[1]);
        Path outputProfileFile = Path.of(args[2]);
        Path notesFile = args.length >= 4 ? Path.of(args[3]) : defaultNotesPath(outputProfileFile);

        var baseProfile = RoutingProfileJsonCodec.load(baseProfileFile);
        var routingResult = SorterDumpRoutingAnalyzer.analyze(baseProfile, dumpFile);
        var suggestionResult = SorterDumpRoutingSuggestionAnalyzer.analyze(baseProfile, routingResult);

        var generator = new HeuristicRoutingProfileGenerator();
        var generationResult = generator.generate(ProfileGenerationRequest.defaults(baseProfile, routingResult, suggestionResult));

        RoutingProfileJsonCodec.write(generationResult.generatedProfile(), outputProfileFile);
        writeNotes(generationResult, notesFile);

        System.out.println("Loaded base profile: " + baseProfileFile);
        System.out.println("Analyzed dump: " + dumpFile);
        System.out.println("Wrote generated profile: " + outputProfileFile);
        System.out.println("Wrote generation notes: " + notesFile);
    }

    private static void writeNotes(com.knightcode.appliedstoragesorter.profilegen.ProfileGenerationResult result, Path notesFile)
            throws IOException {
        if (notesFile.getParent() != null) {
            Files.createDirectories(notesFile.getParent());
        }
        Files.writeString(notesFile, String.join(System.lineSeparator(), result.notes()) + System.lineSeparator());
    }

    private static Path defaultNotesPath(Path outputProfileFile) {
        String fileName = outputProfileFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot >= 0 ? fileName.substring(0, dot) : fileName;
        return outputProfileFile.resolveSibling(baseName + ".notes.txt");
    }
}
