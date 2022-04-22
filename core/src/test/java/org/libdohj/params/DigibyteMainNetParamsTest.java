/*
 * Copyright 2015 J. Ross Nicoll
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
package org.libdohj.params;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DigibyteMainNetParamsTest {
    private static final DigibyteMainNetParams params = DigibyteMainNetParams.get();

    @Before
    public void setUp() throws Exception {
        Context context = new Context(params);
    }

    @Test
    public void testMainNetGenesisBlock() {
        Block genesis = params.getGenesisBlock();
        assertEquals("7497ea1b465eb39f1c8f507bc877078fe016d6fcb6dfad3a64c98dcc6e1e8496", genesis.getHashAsString());
    }
}
