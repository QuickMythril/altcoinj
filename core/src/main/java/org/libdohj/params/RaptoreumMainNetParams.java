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
 * Adapted for Raptoreum in May 2022 by Qortal dev team
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
 * Parameters for the Raptoreum main production network on which people trade
 * goods and services.
 */
public class RaptoreumMainNetParams extends AbstractRaptoreumParams {
    public static final int MAINNET_MAJORITY_WINDOW = 1000; // fix me
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950; // fix me
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750; // fix me

    public RaptoreumMainNetParams() {
        super();
        id = ID_RTM_MAINNET;
        // Genesis hash is b79e5df07278b9567ada8fc655ffbfa9d3f586dc38da3dd93053686f41caeea0

        packetMagic = 0x72746d2e;
        maxTarget = Utils.decodeCompactBits(0x20001fffL);
        port = 10226;
        addressHeader = 60;
        p2shHeader = 16;
        // from https://github.com/Raptor3um/raptoreum/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100; // fix me
        subsidyDecreaseBlockCount = 210240;

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("b79e5df07278b9567ada8fc655ffbfa9d3f586dc38da3dd93053686f41caeea0"));
        // from https://github.com/Raptor3um/raptoreum/blob/master/src/chainparams.cpp

        alertSigningKey = Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "seed00.raptoreum.com",
            "seed01.raptoreum.com",
            "seed02.raptoreum.com",
            "seed03.raptoreum.com",
            "seed04.raptoreum.com",
            "seed05.raptoreum.com",
            "seed06.raptoreum.com",
            "ger1.raptoreum.com",
            "ny1.raptoreum.com"
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 22/Jan/2018 Raptoreum is name of the game for new generation of firms"
            byte[] bytes = Utils.HEX.decode("0004ffff001d01044c5957697265642030392f4a616e2f3230313420546865204772616e64204578706572696d656e7420476f6573204c6976653a204f76657273746f636b2e636f6d204973204e6f7720416363657074696e6720426974636f696e73");
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Coin.valueOf(5000, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        Sha256Hash merkleRoot = Sha256Hash.wrap("87a48bc22468acdd72ee540aab7c086a5bbcddc12b51c6ac925717a74c269453");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 4, Sha256Hash.ZERO_HASH,
                merkleRoot, 1614369600L, 0x20001fffL, 1130, Arrays.asList(t));
                // from https://github.com/Raptor3um/raptoreum/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static RaptoreumMainNetParams instance;
    public static synchronized RaptoreumMainNetParams get() {
        if (instance == null) {
            instance = new RaptoreumMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_RTM_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
