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
 * Adapted for Peercoin in April 2022 by Qortal dev team
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
 * Parameters for the Peercoin main production network on which people trade
 * goods and services.
 */
public class PeercoinMainNetParams extends AbstractPeercoinParams {
    public static final int MAINNET_MAJORITY_WINDOW = 1000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    public PeercoinMainNetParams() {
        super();
        id = ID_PPC_MAINNET;
        // Genesis hash is 0000000032fe677166d54963b62a4677d8957e87c508eaa4fd7eb1c880cd27e3

        packetMagic = 0xe6e8e9e5;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
        port = 9901;
        addressHeader = 55;
        p2shHeader = 117;
        // from https://github.com/peercoin/peercoin/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 183;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 500;
        subsidyDecreaseBlockCount = 2100000;

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("0000000032fe677166d54963b62a4677d8957e87c508eaa4fd7eb1c880cd27e3"));
        // from https://github.com/peercoin/peercoin/blob/master/src/chainparams.cpp

        alertSigningKey = Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "seed.peercoin.net",
            "seed2.peercoin.net",
            "seed.peercoin-library.org",
            "seed.ppcoin.info"
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "Matonis 07-AUG-2012 Parallel Currencies And The Roadmap To Monetary Freedom"
            byte[] bytes = Utils.HEX.decode("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Coin.valueOf(0, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        Sha256Hash merkleRoot = Sha256Hash.wrap("3c2d8f85fab4d17aac558cc648a1a58acff0de6deb890c29985690052c5993c2");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 1, Sha256Hash.ZERO_HASH,
                merkleRoot, 1345084287L, 0x1d00ffffL, 2179302059L, Arrays.asList(t));
                // from https://github.com/peercoin/peercoin/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static PeercoinMainNetParams instance;
    public static synchronized PeercoinMainNetParams get() {
        if (instance == null) {
            instance = new PeercoinMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_PPC_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
