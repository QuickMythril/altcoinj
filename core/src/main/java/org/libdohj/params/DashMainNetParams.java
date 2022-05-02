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
 * Adapted for Dash in May 2022 by Qortal dev team
 * Thanks to https://github.com/dashevo/dashj for the references
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
 * Parameters for the Dash main production network on which people trade
 * goods and services.
 */
public class DashMainNetParams extends AbstractDashParams {
    public static final int MAINNET_MAJORITY_WINDOW = 1000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    public DashMainNetParams() {
        super();
        id = ID_DASH_MAINNET;
        // Genesis hash is 00000ffd590b1485b3caadc19b22e6379c733355108f107a430458cdf3407ab6

        packetMagic = 0xbf0c6bbd;
        maxTarget = Utils.decodeCompactBits(0x1e0fffffL);
        port = 9999;
        addressHeader = 76;
        p2shHeader = 16;
        // from https://github.com/dashpay/dash/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 204;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210240;

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("00000ffd590b1485b3caadc19b22e6379c733355108f107a430458cdf3407ab6"));
        // from https://github.com/dashpay/dash/blob/master/src/chainparams.cpp

        alertSigningKey = Hex.decode("040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "dnsseed.dash.org",
            "dnsseed.dashdot.io"
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
		// dip14HeaderP2PKHpub = 0x0eecefc5; // The 4 byte header that serializes in base58 to "dpmp".
        // dip14HeaderP2PKHpriv = 0x0eecf02e; // The 4 byte header that serializes in base58 to "dpms"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "Wired 09/Jan/2014 The Grand Experiment Goes Live: Overstock.com Is Now Accepting Bitcoins"
            byte[] bytes = Utils.HEX.decode("04ffff001d01044c5957697265642030392f4a616e2f3230313420546865204772616e64204578706572696d656e7420476f6573204c6976653a204f76657273746f636b2e636f6d204973204e6f7720416363657074696e6720426974636f696e73");
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Coin.valueOf(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        Sha256Hash merkleRoot = Sha256Hash.wrap("e0028eb9648db56b1ac77cf090b99048a8007e2bb64b68f092c03c7f56a662c7");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 1, Sha256Hash.ZERO_HASH,
                merkleRoot, 1390095618L, 0x1e0ffff0L, 28917698, Arrays.asList(t));
                // from https://github.com/dashpay/dash/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static DashMainNetParams instance;
    public static synchronized DashMainNetParams get() {
        if (instance == null) {
            instance = new DashMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_DASH_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
