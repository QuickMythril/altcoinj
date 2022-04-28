/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Adapted for BitcoinCash in April 2022 by Qortal dev team
 * Thanks to https://github.com/pokkst/bitcoincashj for the references
 */

package org.libdohj.params;

import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.utils.MonetaryFormat;
import org.libdohj.core.AltcoinNetworkParameters;
import org.libdohj.core.AltcoinSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static org.bitcoinj.core.Coin.COIN;

/**
 * Common parameters for BitcoinCash networks.
 */
public abstract class AbstractBitcoinCashParams extends NetworkParameters implements AltcoinNetworkParameters {
    /** Standard format for the BCH denomination. */
    public static final MonetaryFormat BCH;
    /** Standard format for the mBCH denomination. */
    public static final MonetaryFormat MBCH;
    /** Standard format for the "Satoshi" denomination. */
    public static final MonetaryFormat SATOSHI;

    public static final int BITCOINCASH_TARGET_TIMESPAN = (int)(14 * 24 * 60 * 60);  // 14 days
    public static final int BITCOINCASH_TARGET_SPACING = (int)(10 * 60);  // 10 minutes
    public static final int BITCOINCASH_INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;

    /**
     * The maximum number of coins to be generated
     */
    public static final long MAX_COINS = 21000000;

    /**
     * The maximum money to be generated
     */
    public static final Coin MAX_BITCOINCASH_MONEY = COIN.multiply(MAX_COINS);

    /** Currency code for base 1 BitcoinCash. */
    public static final String CODE_BCH = "BCH";
    /** Currency code for base 1/1,000 BitcoinCash. */
    public static final String CODE_MBCH = "mBCH";
    /** Currency code for base 1/100,000,000 BitcoinCash. */
    public static final String CODE_SATOSHI = "Satoshi";

    static {
        BCH = MonetaryFormat.BTC.noCode()
            .code(0, CODE_BCH)
            .code(3, CODE_MBCH)
            .code(7, CODE_SATOSHI);
        MBCH = BCH.shift(3).minDecimals(2).optionalDecimals(2);
        SATOSHI = BCH.shift(7).minDecimals(0).optionalDecimals(2);
    }

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_BCH_MAINNET = "main";
    /** The string returned by getId() for the testnet. */
    public static final String ID_BCH_TESTNET = "test";
    /** The string returned by getId() for regtest. */
    public static final String ID_BCH_REGTEST = "regtest";

    public static final int BITCOINCASH_PROTOCOL_VERSION_MINIMUM = 70000;
    public static final int BITCOINCASH_PROTOCOL_VERSION_CURRENT = 70013;
    private static final Coin BASE_SUBSIDY = Coin.valueOf(50, 0);

    protected Logger log = LoggerFactory.getLogger(AbstractBitcoinCashParams.class);

    public AbstractBitcoinCashParams() {
        super();
        interval = BITCOINCASH_INTERVAL;
        targetTimespan = BITCOINCASH_TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);

        packetMagic = 0xe3e1f3e8;
        bip32HeaderP2PKHpub = 0x0488B21E; //The 4 byte header that serializes in base58 to "xpub"
        bip32HeaderP2PKHpriv = 0x0488ADE4; //The 4 byte header that serializes in base58 to "xprv"
    }

    @Override
    public Coin getBlockSubsidy(final int height) {
        return BASE_SUBSIDY.shiftRight(height / getSubsidyDecreaseBlockCount());
    }

    /**
     * Get the hash to use for a block.
     */
    @Override
    public Sha256Hash getBlockDifficultyHash(Block block) {
        return ((AltcoinBlock) block).getScryptHash();
    }

    public MonetaryFormat getMonetaryFormat() {
        return BCH;
    }

    @Override
    public Coin getMaxMoney() {
        return MAX_BITCOINCASH_MONEY;
    }

    @Override
    public Coin getMinNonDustOutput() {
        return Coin.valueOf(10000); // fixme
    }

    @Override
    public String getUriScheme() {
        return "bitcoinCash";
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }


    @Override
    public void checkDifficultyTransitions(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore)
        throws VerificationException, BlockStoreException {
        try {
            final long newTargetCompact = calculateNewDifficultyTarget(storedPrev, nextBlock, blockStore);
            final long receivedTargetCompact = nextBlock.getDifficultyTarget();

            if (newTargetCompact != receivedTargetCompact)
                throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                        newTargetCompact + " vs " + receivedTargetCompact);
        } catch (CheckpointEncounteredException ex) {
            // Just have to take it on trust then
        }
    }

    /**
     * Get the difficulty target expected for the next block. This includes all
     * the weird cases for Litecoin such as testnet blocks which can be maximum
     * difficulty if the block interval is high enough.
     * TODO: this may need updating for BitcoinCash; it is currently copied from Litecoin
     *
     * @throws CheckpointEncounteredException if a checkpoint is encountered while
     * calculating difficulty target, and therefore no conclusive answer can
     * be provided.
     */
    public long calculateNewDifficultyTarget(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore)
        throws VerificationException, BlockStoreException, CheckpointEncounteredException {
        final Block prev = storedPrev.getHeader();
        final int previousHeight = storedPrev.getHeight();
        final int retargetInterval = this.getInterval();

        // Is this supposed to be a difficulty transition point?
        if ((storedPrev.getHeight() + 1) % retargetInterval != 0) {
            if (this.allowMinDifficultyBlocks()) {
                // Special difficulty rule for testnet:
                // If the new block's timestamp is more than 5 minutes
                // then allow mining of a min-difficulty block.
                if (nextBlock.getTimeSeconds() > prev.getTimeSeconds() + getTargetSpacing() * 2) {
                    return Utils.encodeCompactBits(maxTarget);
                } else {
                    // Return the last non-special-min-difficulty-rules-block
                    StoredBlock cursor = storedPrev;

                    while (cursor.getHeight() % retargetInterval != 0
                            && cursor.getHeader().getDifficultyTarget() == Utils.encodeCompactBits(this.getMaxTarget())) {
                        StoredBlock prevCursor = cursor.getPrev(blockStore);
                        if (prevCursor == null) {
                            break;
                        }
                        cursor = prevCursor;
                    }

                    return cursor.getHeader().getDifficultyTarget();
                }
            }

            // No ... so check the difficulty didn't actually change.
            return prev.getDifficultyTarget();
        }

        // We need to find a block far back in the chain. It's OK that this is expensive because it only occurs every
        // two weeks after the initial block chain download.
        StoredBlock cursor = storedPrev;
        int goBack = retargetInterval - 1;

        // Litecoin: This fixes an issue where a 51% attack can change difficulty at will.
        // Go back the full period unless it's the first retarget after genesis.
        // Code based on original by Art Forz
        if (cursor.getHeight()+1 != retargetInterval)
            goBack = retargetInterval;

        for (int i = 0; i < goBack; i++) {
            if (cursor == null) {
                // This should never happen. If it does, it means we are following an incorrect or busted chain.
                throw new VerificationException(
                        "Difficulty transition point but we did not find a way back to the genesis block.");
            }
            cursor = blockStore.get(cursor.getHeader().getPrevBlockHash());
        }

        //We used checkpoints...
        if (cursor == null) {
            log.debug("Difficulty transition: Hit checkpoint!");
            throw new CheckpointEncounteredException();
        }

        Block blockIntervalAgo = cursor.getHeader();
        return this.calculateNewDifficultyTargetInner(previousHeight, prev.getTimeSeconds(),
            prev.getDifficultyTarget(), blockIntervalAgo.getTimeSeconds(),
            nextBlock.getDifficultyTarget());
    }

    /**
     * Calculate the difficulty target expected for the next block after a normal
     * recalculation interval. Does not handle special cases such as testnet blocks
     * being setting the target to maximum for blocks after a long interval.
     *
     * @param previousHeight height of the block immediately before the retarget.
     * @param prev the block immediately before the retarget block.
     * @param nextBlock the block the retarget happens at.
     * @param blockIntervalAgo The last retarget block.
     * @return New difficulty target as compact bytes.
     */
    protected long calculateNewDifficultyTargetInner(int previousHeight, final Block prev,
            final Block nextBlock, final Block blockIntervalAgo) {
        return this.calculateNewDifficultyTargetInner(previousHeight, prev.getTimeSeconds(),
            prev.getDifficultyTarget(), blockIntervalAgo.getTimeSeconds(),
            nextBlock.getDifficultyTarget());
    }

    /**
     *
     * @param previousHeight Height of the block immediately previous to the one we're calculating difficulty of.
     * @param previousBlockTime Time of the block immediately previous to the one we're calculating difficulty of.
     * @param lastDifficultyTarget Compact difficulty target of the last retarget block.
     * @param lastRetargetTime Time of the last difficulty retarget.
     * @param nextDifficultyTarget The expected difficulty target of the next
     * block, used for determining precision of the result.
     * @return New difficulty target as compact bytes.
     */
    protected long calculateNewDifficultyTargetInner(int previousHeight, long previousBlockTime,
        final long lastDifficultyTarget, final long lastRetargetTime,
        final long nextDifficultyTarget) {
        final int retargetTimespan = this.getTargetTimespan();
        int actualTime = (int) (previousBlockTime - lastRetargetTime);
        final int minTimespan = retargetTimespan / 4;
        final int maxTimespan = retargetTimespan * 4;

        actualTime = Math.min(maxTimespan, Math.max(minTimespan, actualTime));

        BigInteger newTarget = Utils.decodeCompactBits(lastDifficultyTarget);
        newTarget = newTarget.multiply(BigInteger.valueOf(actualTime));
        newTarget = newTarget.divide(BigInteger.valueOf(retargetTimespan));

        if (newTarget.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newTarget.toString(16));
            newTarget = this.getMaxTarget();
        }

        int accuracyBytes = (int) (nextDifficultyTarget >>> 24) - 3;

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newTarget = newTarget.and(mask);
        return Utils.encodeCompactBits(newTarget);
    }

    @Override
    public AltcoinSerializer getSerializer(boolean parseRetain) {
        return new AltcoinSerializer(this, parseRetain);
    }

    @Override
    public int getProtocolVersionNum(final ProtocolVersion version) {
        switch (version) {
            case PONG:
            case BLOOM_FILTER:
                return version.getBitcoinProtocolVersion();
            case CURRENT:
                return BITCOINCASH_PROTOCOL_VERSION_CURRENT;
            case MINIMUM:
            default:
                return BITCOINCASH_PROTOCOL_VERSION_MINIMUM;
        }
    }

    /**
     * Whether this network has special rules to enable minimum difficulty blocks
     * after a long interval between two blocks (i.e. testnet).
     */
    public boolean allowMinDifficultyBlocks() {
        return this.isTestNet();
    }

    public int getTargetSpacing() {
        return this.getTargetTimespan() / this.getInterval();
    }

    private static class CheckpointEncounteredException extends Exception {  }
}
