[![Apache-2](https://img.shields.io/badge/license-Apache-2.svg)](COPYING)
[![](https://jitpack.io/v/jjos2372/altcoinj.svg)](https://jitpack.io/#jjos2372/altcoinj)


### Welcome to altcoinj

The altcoinj library (forked from [libdohj](https://github.com/dogecoin/libdohj)) is a lightweight
Java library around [bitcoinj](https://bitcoinj.github.io/),
with updated support for Litecoin and Dogecoin.

Pull requests for support for other altcoins would be welcomed.

### Getting started

You should be familiar with [bitcoinj](https://bitcoinj.github.io/) first, as this library simply adds minor
changes to extend bitcoinj. Generally using altcoinj is equivalent to using
bitcoinj, except with different network parameters (reflecting altcoin consensus
in place of Bitcoin).

Add the following to your gradle.build file:
```
repositories {
	maven { url 'https://jitpack.io' }
}
dependencies {
	compile 'com.github.jjos2372:altcoinj:f26e20bb13'
	compile 'org.bitcoinj:bitcoinj-core:0.15.6'
}
```

Be aware however that altcoin blocks have their own class, AltcoinBlock, which
adds support for features such as AuxPoW.

#### Building from the command line

Simply run on the command line
```
./gradlew jar
```

The outputs are under the `build` directory.

#### Building from an IDE

Alternatively, just import the project on your preferred IDE as a *gradle* project.

