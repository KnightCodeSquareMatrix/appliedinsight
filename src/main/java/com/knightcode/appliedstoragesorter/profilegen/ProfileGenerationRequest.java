package com.knightcode.appliedstoragesorter.profilegen;

import com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingAnalyzer;
import com.knightcode.appliedstoragesorter.analysis.SorterDumpRoutingSuggestionAnalyzer;
import com.knightcode.appliedstoragesorter.rule.route.RoutingProfile;

public record ProfileGenerationRequest(
        RoutingProfile baseProfile,
        SorterDumpRoutingAnalyzer.AnalysisResult routingAnalysis,
        SorterDumpRoutingSuggestionAnalyzer.SuggestionResult suggestionAnalysis,
        int maxSuggestedModRules,
        int maxSuggestedItemRules,
        int minimumModFallbackItemCount,
        long minimumExplicitItemAmount) {
    public ProfileGenerationRequest {
        if (maxSuggestedModRules < 0) {
            throw new IllegalArgumentException("maxSuggestedModRules must be >= 0");
        }
        if (maxSuggestedItemRules < 0) {
            throw new IllegalArgumentException("maxSuggestedItemRules must be >= 0");
        }
        if (minimumModFallbackItemCount < 0) {
            throw new IllegalArgumentException("minimumModFallbackItemCount must be >= 0");
        }
        if (minimumExplicitItemAmount < 0) {
            throw new IllegalArgumentException("minimumExplicitItemAmount must be >= 0");
        }
    }

    public static ProfileGenerationRequest defaults(
            RoutingProfile baseProfile,
            SorterDumpRoutingAnalyzer.AnalysisResult routingAnalysis,
            SorterDumpRoutingSuggestionAnalyzer.SuggestionResult suggestionAnalysis) {
        return new ProfileGenerationRequest(baseProfile, routingAnalysis, suggestionAnalysis, 5, 5, 3, 512L);
    }
}
