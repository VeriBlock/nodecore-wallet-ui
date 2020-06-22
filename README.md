# VeriBlock GUI Wallet

## Overview
The VeriBlock Wallet GUI provides a way to control your wallets through a very simple interface.

More details on the wiki: https://wiki.veriblock.org/index.php?title=GUI_Wallet

## Requisites
* A local NodeCore v0.4.9 instance (or access to a remote one)
* OpenJDK 13 (You can download it here: https://openjdk.java.net/projects/jdk/)

## Features & Capabilities
The Wallet GUI contains the next features and capabilities:

* Send transactions
* View transactions
* Control your addresses
    * Add nicknames to an address
    * Set a default address
* Backup and import wallets
* Manage multiple addresses
* Set a language
* Copy any cell from the wallet grids (transactions, addresses... etc)
* Manage the NodeCore connection state

### Building from the command line

To perform a full build, run the following

    ./gradlew clean build (linux/mac)
    ./gradlew.bat clean build (windows)
    
### Troubleshooting
If you get this error, then you probably need to install OpenJdk13+:
```
Exception in thread "main" java.lang.UnsupportedClassVersionError
```
