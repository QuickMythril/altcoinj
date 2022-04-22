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
 * Adapted for Digibyte in April 2022 by Qortal dev team
 * Thanks to https://github.com/DigiByte-Core/digibytej for the references
 */

package org.libdohj.params;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptOpCodes;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;

import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.core.Coin.COIN;

/**
 * Parameters for the Digibyte main production network on which people trade
 * goods and services.
 */
public class DigibyteMainNetParams extends AbstractDigibyteParams {
    public static final int MAINNET_MAJORITY_WINDOW = MainNetParams.MAINNET_MAJORITY_WINDOW;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = MainNetParams.MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = MainNetParams.MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;

    public DigibyteMainNetParams() {
        super();
        id = ID_DGB_MAINNET;
        // Genesis hash is 7497ea1b465eb39f1c8f507bc877078fe016d6fcb6dfad3a64c98dcc6e1e8496
        packetMagic = 0xfac3b6da;

        maxTarget = Utils.decodeCompactBits(0x1e0fffffL);
        port = 12024;
        addressHeader = 30;
        p2shHeader = 63;
        segwitAddressHrp = "dgb";
        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 128;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 210000;

        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("7497ea1b465eb39f1c8f507bc877078fe016d6fcb6dfad3a64c98dcc6e1e8496"));
        alertSigningKey = Hex.decode("040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "seed1.digibyte.io",
            "seed2.digibyte.io",
            "seed3.digibyte.io",
            "seed.digibyte.io",
            "digihash.co",
            "digiexplorer.info",
            "seed.digibyteprojects.com"
        };
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
        bip32HeaderP2WPKHpub = 0x04b24746; // The 4 byte header that serializes in base58 to "zpub".
        bip32HeaderP2WPKHpriv = 0x04b2430c; // The 4 byte header that serializes in base58 to "zprv"
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        AltcoinBlock genesisBlock = new AltcoinBlock(params, Block.BLOCK_VERSION_GENESIS);
        Transaction t = new Transaction(params);
        try {
            byte[] bytes = Hex.decode
                    ("04ffff001d0104404e592054696d65732030352f4f63742f32303131205374657665204a6f62732c204170706c65e280997320566973696f6e6172792c2044696573206174203536");
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Hex.decode
                    ("040184710fa689ad5023690c80f3a49c8f13f8d45b8c857fbcbc8bc4a8e4d3eb4b10f4d4604fa08dce601aaf0f470216fe1b51850b4acf21b179c45070ac7b03a9"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, COIN.multiply(50), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        genesisBlock.setTime(1317972665L);
        genesisBlock.setDifficultyTarget(0x1e0ffff0L);
        genesisBlock.setNonce(2084524493);
        return genesisBlock;
    }

    private static DigibyteMainNetParams instance;
    public static synchronized DigibyteMainNetParams get() {
        if (instance == null) {
            instance = new DigibyteMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_DGB_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
