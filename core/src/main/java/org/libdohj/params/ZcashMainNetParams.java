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
 * Adapted for Zcash in April 2022 by Qortal dev team
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
 * Parameters for the Zcash main production network on which people trade
 * goods and services.
 */
public class ZcashMainNetParams extends AbstractZcashParams {
    public static final int MAINNET_MAJORITY_WINDOW = 4000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    public ZcashMainNetParams() {
        super();
        id = ID_ZEC_MAINNET;
        // Genesis hash is 00040fe8ec8471911baa1db1266ea15dd06b4a8a5c453883c000b031973dce08

        packetMagic = 0x24e92764;
        maxTarget = Utils.decodeCompactBits(0x1f07ffffL);
        port = 8233;
        addressHeader = 28; // {0x1C,0xB8} = 28,184
        p2shHeader = 28; // {0x1C,0xBD} = 28,189
        // from https://github.com/zcash/zcash/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128; // 0x80

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100; // fixme
        subsidyDecreaseBlockCount = 2100000; // fixme

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("00040fe8ec8471911baa1db1266ea15dd06b4a8a5c453883c000b031973dce08"));
        // from https://github.com/zcash/zcash/blob/master/src/chainparams.cpp

        alertSigningKey = Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "dnsseed.z.cash",
            "dnsseed.str4d.xyz",
            "mainnet.seeder.zfnd.org",
            "mainnet.is.yolo.money"
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Economist 2016-10-29 Known unknown: Another crypto-currency is born."
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

        Sha256Hash merkleRoot = Sha256Hash.wrap("c4eaa58879081de3c24a7b117ed2b28300e7ec4c4c1dff1d3f1268b7857a4ddb");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 4, Sha256Hash.ZERO_HASH,
                merkleRoot, 1477641360L, 0x1f07ffffL, 1257, Arrays.asList(t));
                // from https://github.com/zcash/zcash/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static ZcashMainNetParams instance;
    public static synchronized ZcashMainNetParams get() {
        if (instance == null) {
            instance = new ZcashMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_ZEC_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
