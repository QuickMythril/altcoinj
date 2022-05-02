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
 * Adapted for Namecoin in May 2022 by Qortal dev team
 * Thanks to https://github.com/dogecoin/libdohj & https://github.com/jjos2372/altcoinj for the references
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
 * Parameters for the Namecoin main production network on which people trade
 * goods and services.
 */
public class NamecoinMainNetParams extends AbstractNamecoinParams {
    public static final int MAINNET_MAJORITY_WINDOW = 1000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    public NamecoinMainNetParams() {
        super();
        id = ID_NMC_MAINNET;
        // Genesis hash is 000000000062b72c5e2ceb45fbc8587e807c155b0da735e6483dfba2f0a9c770

        packetMagic = 0xf9beb4fe;
        maxTarget = Utils.decodeCompactBits(0x1c007fffL);
        port = 8334;
        addressHeader = 52;
        p2shHeader = 13;
        // from https://github.com/namecoin/namecoin-core/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 180;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;
		auxpowStartHeight = 19200;

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("000000000062b72c5e2ceb45fbc8587e807c155b0da735e6483dfba2f0a9c770"));
        // from https://github.com/namecoin/namecoin-core/blob/master/src/chainparams.cpp

        alertSigningKey = Hex.decode("04ba207043c1575208f08ea6ac27ed2aedd4f84e70b874db129acb08e6109a3bbb7c479ae22565973ebf0ac0391514511a22cb9345bdb772be20cfbd38be578b0c");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "seed.namecoin.libreisp.se",
            "nmc.seed.quisquis.de",
            "seed.nmc.markasoftware.com",
            "dnsseed1.nmc.dotbit.zone",
            "dnsseed2.nmc.dotbit.zone",
            "dnsseed.nmc.testls.space",
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "... choose what comes next.  Lives of your own, or a return to chains. -- V"
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

        Sha256Hash merkleRoot = Sha256Hash.wrap("41c62dbd9068c89a449525e3cd5ac61b20ece28c3c38b3f35b2161f0e6d3cb0d");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 1, Sha256Hash.ZERO_HASH,
                merkleRoot, 1303000001L, 0x1c007fffL, 2719916434L, Arrays.asList(t)); // 2719916434L = 0xa21ea192u
                // from https://github.com/namecoin/namecoin-core/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static NamecoinMainNetParams instance;
    public static synchronized NamecoinMainNetParams get() {
        if (instance == null) {
            instance = new NamecoinMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_NMC_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
