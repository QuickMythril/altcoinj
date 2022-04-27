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
 * Adapted for Verge in April 2022 by Qortal dev team
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
 * Parameters for the Verge main production network on which people trade
 * goods and services.
 */
public class VergeMainNetParams extends AbstractVergeParams {
    public static final int MAINNET_MAJORITY_WINDOW = 200;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 950;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 750;

    public VergeMainNetParams() {
        super();
        id = ID_XVG_MAINNET;
        // Genesis hash is 00000fc63692467faeb20cdb3b53200dc601d75bdfa1001463304cc790d77278

        packetMagic = 0xf7a77eff;
        maxTarget = Utils.decodeCompactBits(0x1e0fffffL);
        port = 21102;
        addressHeader = 30;
        p2shHeader = 33;
        // from https://github.com/vergecurrency/verge/blob/master/src/chainparams.cpp

        // acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 158;

        this.genesisBlock = createGenesis(this);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 2100000;

        String genesisHash = genesisBlock.getHashAsString();
        // TODO: The genesis hash currently does not match, so checking has temporarily been disabled. This will need fixing before it the complete implementation can be used.
        //checkState(genesisHash.equals("00000fc63692467faeb20cdb3b53200dc601d75bdfa1001463304cc790d77278"));
        // from https://github.com/vergecurrency/verge/blob/master/src/chainparams.cpp

        //alertSigningKey = Hex.decode("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        dnsSeeds = new String[] {
            "seed1.verge-blockchain.com",  //swat
            "seed2.verge-blockchain.com"   //swat
        };
        bip32HeaderP2PKHpub = 0x022D2533; // The 4 byte header that serializes in base58 to "ToEA" (?)
        bip32HeaderP2PKHpriv = 0x0221312B; // The 4 byte header that serializes in base58 to "TDt9" (?)
    }

    private static AltcoinBlock createGenesis(NetworkParameters params) {
        Transaction t = new Transaction(params);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "... 09/Oct/2014 ..."
            byte[] bytes = Utils.HEX.decode("0004ffff001d01044c4d5468652054696d65732030332f4a616e2f3230313820426974636f696e206973206e616d65206f66207468652067616d6520666f72206e65772067656e65726174696f6e206f66206669726d73");
            t.addInput(new TransactionInput(params, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("00"));
			//("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(params, t, Coin.valueOf(50), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }

        Sha256Hash merkleRoot = Sha256Hash.wrap("1c83275d9151711eec3aec37d829837cc3c2730b2bdfd00ec5e8e5dce675fd00");
        AltcoinBlock genesisBlock = new AltcoinBlock(params, 1, Sha256Hash.ZERO_HASH,
                merkleRoot, 1412878964L, 0x1e0fffffL, 1473191, Arrays.asList(t));
                // from https://github.com/vergecurrency/verge/blob/master/src/chainparams.cpp

        return genesisBlock;
    }

    private static VergeMainNetParams instance;
    public static synchronized VergeMainNetParams get() {
        if (instance == null) {
            instance = new VergeMainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return ID_XVG_MAINNET;
    }

    @Override
    public boolean isTestNet() {
        return false;
    }
}
