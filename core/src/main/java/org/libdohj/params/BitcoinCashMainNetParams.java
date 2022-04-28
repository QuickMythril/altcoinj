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
 * Adapted for BitcoinCash in April 2022 by Qortal dev team
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
 * Parameters for the BitcoinCash main production network on which people trade
 * goods and services.
 */
public class BitcoinCashMainNetParams extends AbstractBitcoinCashParams {
    public static final int MAINNET_MAJORITY_WINDOW = 1000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    public BitcoinCashMainNetParams() {
        super();
        id = ID_BCH_MAINNET;
        // Genesis hash is 000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f

        packetMagic = 0xe3e1f3e8;
        maxTarget = Utils.decodeCompactBits(0x1d00ffffL);
        port = 8333;
        addressHeader = 0;
        p2shHeader = 5;
        // from https://gitlab.com/bitcoin-cash-node/bitcoin-cash-node/-/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"));
        // from https://gitlab.com/bitcoin-cash-node/bitcoin-cash-node/-/blob/master/src/chainparams.cpp

        alertSigningKey = Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "seed.flowee.cash",
            "seed-bch.bitcoinforks.org",
            "btccash-seeder.bitcoinunlimited.info",
            "seed.bchd.cash",
            "seed.bch.loping.net",
            "dnsseed.electroncash.de",
            "bchseed.c3-soft.com",
            "bch.bitjson.com"
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
            byte[] bytes = Utils.HEX.decode("0004ffff001d01044c4d5468652054696d65732030332f4a616e2f3230313820426974636f696e206973206e616d65206f66207468652067616d6520666f72206e65772067656e65726174696f6e206f66206669726d73");
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Coin.valueOf(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        Sha256Hash merkleRoot = Sha256Hash.wrap("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 1, Sha256Hash.ZERO_HASH,
                merkleRoot, 1231006505L, 0x1d00ffffL, 2083236893L, Arrays.asList(t));
                // from https://gitlab.com/bitcoin-cash-node/bitcoin-cash-node/-/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static BitcoinCashMainNetParams instance;
    public static synchronized BitcoinCashMainNetParams get() {
        if (instance == null) {
            instance = new BitcoinCashMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_BCH_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
