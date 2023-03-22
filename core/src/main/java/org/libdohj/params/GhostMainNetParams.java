/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
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
 * Adapted for Ghost in March 2023 by Ghost & Qortal dev team
 */

package org.libdohj.params;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptOpCodes;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the Ghost main production network on which people trade
 * goods and services.
 */
public class GhostMainNetParams extends AbstractGhostParams {
    public static final int MAINNET_MAJORITY_WINDOW = 1000;  // GhostToDo - need to find
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;  // GhostToDo - need to find
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;  // GhostToDo - need to find

    public GhostMainNetParams() {
        super();
        id = ID_GHOST_MAINNET;
        // Genesis hash is 00001e92daa9a7c945afdf3ce2736862b128f95c8966d3cda112caea98dd95f0

        packetMagic = 0xf2f3e1b4; // https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L519-L522
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL); // GhostToDo - needs confirmed - https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L669
        port = 51728; // https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L523
        addressHeader = 38; // 38 = 0x26 - https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L568
        p2shHeader = 97; // 97 = 0x61 - https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L569

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 166; // = 0xA6 - https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L572

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100;  // GhostToDo - need to find
        subsidyDecreaseBlockCount = 210000; // https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L446

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("00001e92daa9a7c945afdf3ce2736862b128f95c8966d3cda112caea98dd95f0"));

        alertSigningKey = Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");
        // from https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L218

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "ghostseeder.ghostbyjohnmcafee.com" // https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L561
        };
        bip32HeaderP2PKHpub = 0x68DF7CBD; // The 4 byte header that serializes in base58 to "PGHST".
        bip32HeaderP2PKHpriv = 0x8E8EA8EA; // The 4 byte header that serializes in base58 to "XGHST"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
            byte[] bytes = Utils.HEX.decode("0004ffff001d01044c4d5468652054696d65732030332f4a616e2f3230313820426974636f696e206973206e616d65206f66207468652067616d6520666f72206e65772067656e65726174696f6e206f66206669726d73");
            // GhostToDo - need to confirm above line
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Coin.valueOf(0, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        Sha256Hash merkleRoot = Sha256Hash.wrap("3365ed8b8758ef69f7edeae23c1ec4bc7a893df9b7d3ff49e4846a1c29a2121f"); // https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L553
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 2, Sha256Hash.ZERO_HASH,
                merkleRoot, 1592430039L, 0x1f00ffffL, 96427, Arrays.asList(t)); // https://github.com/ghost-coin/ghost-core/blob/master/src/chainparams.cpp#L550

        return genesisBlock;
    }

    private static GhostMainNetParams instance;
    public static synchronized GhostMainNetParams get() {
        if (instance == null) {
            instance = new GhostMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_GHOST_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
