package com.knightcode.appliedstoragesorter.ae2.analysis;

import java.util.List;

public record StorageAnalyzerReport(
        NetworkSummary summary,
        List<StorageLocationSummary> storageLocations,
        List<ItemDistributionSummary> itemDistributions,
        List<String> healthFlags,
        List<ItemDistributionSummary> mostFragmentedItems,
        List<StorageLocationSummary> largestStorages,
        List<ItemDistributionSummary> mixedInternalExternalItems,
        List<StorageSemanticCandidate> suspectedSemanticCandidates) {

    public record NetworkSummary(
            int networkNodeCount,
            int storageLocationCount,
            int nonEmptyStorageLocationCount,
            int internalStorageLocationCount,
            int externalStorageLocationCount,
            long internalTotalAmount,
            long externalTotalAmount,
            int uniqueKeyCount,
            int duplicatedKeyCount,
            long totalAmount,
            int totalOccurrenceCount,
            int internalOnlyKeyCount,
            int externalOnlyKeyCount,
            int mixedLocationKeyCount,
            double fragmentationScore,
            String fragmentationLevel) {
    }

    public record StorageLocationSummary(
            String locationId,
            String sourceBlockId,
            String attachedStorageBlockId,
            String storageKind,
            String hostPos,
            String attachedStoragePos,
            boolean externalStorageBus,
            int slot,
            int distinctKeyCount,
            long totalAmount,
            double networkAmountShare,
            List<KeyAmountSummary> topKeys,
            Long totalBytes,
            Long usedBytes,
            Integer totalItemTypes,
            Integer remainingItemTypes,
            String cellKind,
            String zoneId) {
    }

    public record ItemDistributionSummary(
            String keyType,
            String keyId,
            String displayName,
            long totalAmount,
            int locationCount,
            int internalLocationCount,
            int externalLocationCount,
            long maxSingleLocationAmount,
            int maxStackSize) {
    }

    public record StorageSemanticCandidate(
            String candidateType,
            String suggestedClassification,
            String locationId,
            String sourceBlockId,
            String attachedStorageBlockId,
            String storageKind,
            String hostPos,
            String attachedStoragePos,
            long totalAmount,
            double networkAmountShare,
            double suspicionScore,
            List<String> matchedHeuristics) {
    }

    public record KeyAmountSummary(
            String keyType,
            String keyId,
            String displayName,
            long amount,
            int maxStackSize) {
    }
}
