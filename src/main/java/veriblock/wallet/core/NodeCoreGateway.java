// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.core;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import javafx.util.Pair;

import nodecore.api.grpc.*;

import nodecore.api.grpc.utilities.ByteStringUtility;
import nodecore.api.grpc.utilities.ChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.veriblock.core.utilities.AddressUtility;
import org.veriblock.core.utilities.BlockUtility;
import org.veriblock.core.utilities.Utility;
import veriblock.wallet.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

//Returns entities
public class NodeCoreGateway {

    private static final Logger _logger = LoggerFactory.getLogger(NodeCoreGateway.class);

    //There is a single nodecore instance, therefore make this a singleton

    //region Setup

    private static NodeCoreGateway single_instance = null;

    private NodeCoreGateway() {

    }

    // static method to create instance of Singleton class
    public static NodeCoreGateway getInstance() {
        if (single_instance == null)
            single_instance = new NodeCoreGateway();

        return single_instance;
    }

    //only sets the fields, and will set blockingstub null, does not try to connect
    public void setConnectionInfo(ConnectionInput connectionInput) throws  Exception {
        if (connectionInput == null) {
            throw new Exception("ConnectionInput cannot be null");
        }

        _connectionInput = connectionInput;
        _blockingStub = null;   //no longer valid, so clear it

        _logger.info("NodeCoreGateway set connection: {}:{}, hasPassword={}",
                connectionInput.getAddress(),
                connectionInput.getPort(),
                connectionInput.hasPassword());
    }

    private ConnectionInput _connectionInput;

    //only need 1 channel?
    private AdminGrpc.AdminBlockingStub _blockingStub;

    private void clearBlockingStub() {
        //TODO - thread safety
        _blockingStub = null;
    }

    private void setBlockingStub(AdminGrpc.AdminBlockingStub blockingStub) {
        //TODO - thread safety
        _blockingStub = blockingStub;
    }

    private AdminGrpc.AdminBlockingStub getBlockingStub() {
        //TODO - thread safety
        return _blockingStub;
    }

    //Merely having a non-null blockingStub does not mean it is actually connected, or that NC is in sync
    private Pair<ConnectionResult, RpcGetStateInfoReply> ensureBlockingStub() {

        ConnectionResult result = new ConnectionResult(_connectionInput);
        Pair<ConnectionResult, RpcGetStateInfoReply> outputs = new
                Pair<>(result, null);

        //are inputs specified? Cannot proceed further
        if (!_connectionInput.hasConnectionInfo()) {
            //Will need to specify before proceeding
            result.ConnectionState = ConnectionState.NotSpecified;
            clearBlockingStub();
            return outputs;
        }

        //set NC version at the very core
        result.NodeCoreVersion = outputs.getKey().NodeCoreVersion;

        //We have inputs, so we can at least try connecting...
        if (getBlockingStub() == null) {
            //Doesn't exist, so create it:
            try {
                //Can create this without password
                ChannelBuilder channelBuilder = new ChannelBuilder(createRpcConfiguration());
                ManagedChannel channel = channelBuilder.buildManagedChannel();

                Channel interceptorChannel = channelBuilder.attachPasswordInterceptor(channel);
                setBlockingStub(AdminGrpc.newBlockingStub(interceptorChannel));
            } catch (Exception ex) {
                //Bad! Couldn't even create stub, let alone connect, let alone in sync
                _logger.info("Could not create blockingStub {}", ex);
                result.ConnectionState = ConnectionState.ErrorCouldNotConnect;
                result.Exception = ex;
                return outputs;
            }
        }

        //Have a blocking stub (could have been created before) --> can we do lightweight ping?
        try {
            //Password needed here
            RpcGetStateInfoReply testGetStateInfo = getBlockingStub().getStateInfo(
                    RpcGetStateInfoRequest.newBuilder().build());

            outputs = new Pair<>(result, testGetStateInfo);
            //We could at least connect!
            //Now check if in sync
            boolean isInSync = isInSync(testGetStateInfo);
            if (isInSync) {
                result.ConnectionState = ConnectionState.Connected_Synced;
            } else {
                result.ConnectionState = ConnectionState.Connected_Syncing;
            }

        } catch (StatusRuntimeException exGrpc) {
            result.setErrorCode(exGrpc);

            //If password was invalid, then clear it so we can reprompt for the right one
            if (result.IsErrorBadGrpcPassword)
            {
                this.clearWalletPassword();
            }
        } catch (Exception ex) {
            //Bad, could not connect
            result.ConnectionState = ConnectionState.ErrorCouldNotConnect;
            result.Exception = ex;
        }

        return outputs;
    }

    // TODO: Expand this to handle for SSL and certificates
    private AdminRpcConfiguration createRpcConfiguration() {
        AdminRpcConfiguration config = new AdminRpcConfiguration();
        config.setNodeCoreHost(_connectionInput.getAddress());
        config.setNodeCorePort(_connectionInput.getPort());
        if (_connectionInput.hasPassword()) {
            config.setNodeCorePassword(_connectionInput.getPassword());
        }
        else
        {
            //clear it
            config.setNodeCorePassword(null);
        }
        return config;
    }

    private boolean isInSync(RpcGetStateInfoReply result) {
        Integer threshold = 3; //local must be no less than X from network to be seen as insync

        //TODO _ alphanet hack --> want alphanet to be synced for easier testing, and network height may be
        //  0 if no peers
        String strNetwork = result.getNetworkVersion().toLowerCase();
        if (strNetwork.equals("alpha")) {
            return true;
        }

        if (result.getNetworkHeight() == 0) {
            //In order to be 0, must not have found peers || the network must still be syncing
            return false;
        }

        Integer localBlockchainHeight = result.getLocalBlockchainHeight();
        Integer networkHeight = result.getNetworkHeight();

        if (localBlockchainHeight + threshold >= networkHeight) {
            //great!
            return true;
        } else {
            //bad - not caught up yet
            return false;
        }
    }


    private class OutputContainer<T> {
        public OutputContainer(T entity, boolean replySuccess, List<RpcResult> replyList) {
            //TODO - could not convert to generic RpcProtocolReply reply2, so pass in parts
            Entity = entity;
            Success = replySuccess;
            ReplyList = replyList;
        }

        public T Entity;
        private boolean Success;
        List<RpcResult> ReplyList;
    }

    private interface DoGrpcWork<T> {
        OutputContainer<T> doWork();
    }

    private <T> CommandResult<T> templateCommand(DoGrpcWork<T> grpcWork)
    {
        boolean walletMustBeUnlocked = false;
        return templateCommand( grpcWork, walletMustBeUnlocked);
    }

    //region Lock Wallet

    public void clearWalletPassword()
    {
        _passphraseKeep = null;
        _passphrase = null;
    }

    /*
    If either short or longterm password is stored, then return true.
     */
    public boolean getIsWalletPasswordStored()
    {
        if (_passphraseKeep != null || _passphrase != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //If we can retrieve the long-term password, no reason to re-request it
    public boolean getIsWalletPasswordLongTerm()
    {
        return (_passphraseKeep != null);
    }
    //lock doesn't erase this
    public void setWalletPasswordLongTerm(String passphrase)
    {
        _passphraseKeep = passphrase;
    }
    //Will be used for one command and then lock erases it
    public void setWalletPasswordOnce(String passphrase)
    {
        _passphrase = passphrase;
    }
    private String _passphraseKeep;
    private String _passphrase;

    //Return true if wallet unlocked
    public boolean unlockWallet()
    {
        String passphrase = _passphrase;
        if (_passphrase == null)
        {
            passphrase = _passphraseKeep;
        }

        CommandResult<Void> result = this.unlockwallet(passphrase);
        return !result.isErrorWalletLocked();
    }

    public CommandResult<Void> unlockwallet(String passphrase) {
        DoGrpcWork<Void> work = () ->
        {
            RpcProtocolReply reply = _blockingStub
                    .unlockWallet(RpcUnlockWalletRequest.newBuilder()
                            .setPassphrase(passphrase)
                            .build());

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };
        CommandResult<Void> finalResult = templateCommand(work);

        //Check if password was invalid. If so, clear it so that caller can enter correct password
        //Error: Passphrase is invalid for unlocking this wallet
        if (finalResult.isErrorWalletLocked())
        {
            clearWalletPassword();
            _logger.info("GRPC unlockwallet (passphrase provided) - failed attempt");
        }
        else
        {
            _logger.info("GRPC unlockwallet (passphrase provided) - sucessfully unlocked");
        }

        return finalResult;
    }

    //Called after unlock wallet, will clear the password from NC cache
    public CommandResult<Void> lockwallet() {
        DoGrpcWork<Void> work = () ->
        {
            RpcProtocolReply reply = _blockingStub
                    .lockWallet(RpcLockWalletRequest.newBuilder().build());

            _logger.info("GRPC lockwallet");

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };

        _passphrase = null;
        return templateCommand(work);
    }

    public boolean isWalletLocked()
    {
        //Wrapper around getStateInfo
        CommandResult<StateInfoEntity> commandResult = this.getGetStateInfo();
        if (commandResult.isSuccess())
        {
            return (commandResult.getPayload().walletLockState == WalletLockState.LOCKED);
        }
        else
        {
            //assume not locked
            return false;
        }
    }

    public CommandResult<Void> encryptWallet(String passphrase) {
        DoGrpcWork<Void> work = () ->
        {
            RpcProtocolReply reply = _blockingStub
                    .encryptWallet(RpcEncryptWalletRequest.newBuilder()
                            .setPassphrase(passphrase)
                            .build());

            _logger.info("GRPC encryptWallet");

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };

        this.clearWalletPassword();
        return templateCommand(work, false);
    }

    public CommandResult<Void> decryptWallet(String passphrase) {
        DoGrpcWork<Void> work = () ->
        {
            RpcProtocolReply reply = _blockingStub
                    .decryptWallet(RpcDecryptWalletRequest.newBuilder()
                            .setPassphrase(passphrase)
                            .build());

            _logger.info("GRPC decryptWallet");

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };

        this.clearWalletPassword();
        return templateCommand(work, false);
    }

    //endregion

    private <T> CommandResult<T> templateCommand(DoGrpcWork<T> grpcWork, boolean walletMustBeUnlocked) {
        CommandResult<T> commandResult = new CommandResult<>();

        //get connection setting here
        Pair<ConnectionResult, RpcGetStateInfoReply> outputs = ensureBlockingStub();
        ConnectionResult connectionResult = outputs.getKey();
        commandResult.setConnectionResult(connectionResult);
        if (!connectionResult.isConnectedAndSynced()) {
            //Not connected (including fully synced), no point to proceeding
            return commandResult;
        }

        try {
            if (walletMustBeUnlocked)
            {
                //a command requiring an unlocked wallet
                this.unlockWallet();
            }
            //CUSTOM - START
            OutputContainer<T> outputContainer = grpcWork.doWork();    //doCustom(sendInput); --> invoke todoInjectMethod
            T entity = outputContainer.Entity;
            boolean replySuccess = outputContainer.Success;
            List<RpcResult> replyList = outputContainer.ReplyList;
            //END CUSTOM - END

            ValidationInfo ri = new ValidationInfo();

            if (replySuccess) {
                //great!
                ri.setStatus(ValidationInfo.Status.Success);
            } else {
                //error, populate ValidationInfo
                ri.setStatus(ValidationInfo.Status.Error);
                StringBuilder sb = new StringBuilder();
                for (RpcResult errorResult : replyList) {
                    sb.append(errorResult.getDetails() + " ");
                }
                ri.setMessageInfo(sb.toString().trim());
            }

            commandResult.setValidationInfo(ri);
            commandResult.setPayload(entity);
        } catch (Exception ex) {
            //Something bad, not related to connection
            commandResult.setValidationInfo(ex);
        }
        finally {
            if (walletMustBeUnlocked)
            {
                //relock wallet back up
                this.lockwallet();
            }
        }

        return commandResult;
    }

    //endregion

    //region Utilities

    private static String formatHash(ByteString input) {
        return Utility.bytesToHex(input.toByteArray());
    }

    private static String formatAddress(ByteString input) {
        return Utility.bytesToBase58(input.toByteArray());
    }

    //endregion

    public boolean isConnected()
    {
        boolean isConnected = false;
        CommandResult<StateInfoEntity> result = getGetStateInfo();

        //Also check minRequired version, check this before we wait for syncing to finish
        if (result.getPayload() != null) {
            String actualVersion = result.getPayload().ProgramVersion;
            String minRequiredVersion = Constants.MINIMUM_NODECORE_VERSION;
            boolean isSufficientNCVersion = Utils.isHigherNodeCoreVersion(actualVersion, minRequiredVersion);
            if (!isSufficientNCVersion) {
                isConnected = false;
                _logger.info("isConnected: {}. ActualVersion={}, MinRequiredVersion={}", isConnected,
                        actualVersion, minRequiredVersion);
                return isConnected;
            }
        }

        if (result.getConnectionResult().isConnectedAndSynced())
        {
            isConnected = true;
        }
        _logger.info("isConnected: {}", isConnected);
        return isConnected;
    }

    //Will be called in background every 2 seconds...
    //Special case, as getStateInfo already called as part of ping
    public CommandResult<StateInfoEntity> getGetStateInfo() {
        _logger.debug("NodeCoreGateway getGetStateInfo - Start");  //Will get better info log further down
        CommandResult<StateInfoEntity> commandResult = new CommandResult<>();

        //get connection setting here
        Pair<ConnectionResult, RpcGetStateInfoReply> outputs = ensureBlockingStub();
        ConnectionResult connectionResult = outputs.getKey();
        commandResult.setConnectionResult(connectionResult);

        //want getStateInfo from the blockingStub test
        RpcGetStateInfoReply gsi = outputs.getValue();


        try {
            //Log connection status
            //Add additional log info:
            String connectionLogMessage = null;
            if (connectionResult.ConnectionState == ConnectionState.ErrorCouldNotConnect) {
                if (connectionResult.Exception != null) {
                    connectionLogMessage = connectionResult.Exception.getMessage();
                }
            }
            else if (connectionResult.ConnectionState == ConnectionState.Connected_Syncing) {
                connectionLogMessage = String.format("Block %1$s of %2$s",
                        gsi.getLocalBlockchainHeight(), gsi.getNetworkHeight());
            }
            //Normal operation = success, don't log this every 2 seconds
            else if (connectionResult.ConnectionState == ConnectionState.Connected_Synced) {
                _logger.debug("ConnectionState({})={}, {}",
                        connectionResult.ConnectionInput.getAddressPort(), connectionResult.ConnectionState, connectionLogMessage);
            }
            else {
                //Do show more detail if not yet synced:
                _logger.info("ConnectionState({})={}, {}",
                        connectionResult.ConnectionInput.getAddressPort(), connectionResult.ConnectionState, connectionLogMessage);
            }

            StateInfoEntity model = new StateInfoEntity();
            commandResult.setPayload(model);
            if (gsi != null) {
                model.LocalBlockchainHeight = gsi.getLocalBlockchainHeight();
                //hack - force a random value
                // + DateTime.now().getMillisOfDay();

                model.NetworkHeight = gsi.getNetworkHeight();
                model.NetworkVersion = gsi.getNetworkVersion();

                model.ProgramVersion = gsi.getProgramVersion();
                model.NodeCoreStartTime = gsi.getNodecoreStarttime();
                model.ConnectedPeerCount = gsi.getConnectedPeerCount();
                model.DataDirectory = gsi.getDataDirectory();
                model.OperatingState = gsi.getOperatingState().getState().name();
                model.WalletCacheSyncHeight = gsi.getWalletCacheSyncHeight();
                model.walletLockState = StateInfoEntity.parseWalletLockState(gsi.getWalletState().toString());
            }
        } catch (Exception ex) {
            //show never be hit, as everthing caught prior
            _logger.info("NodeCoreGateway getGetStateInfo: {}", ex);
            throw ex;
        }
        return commandResult;
    }

    //-----------------------------

    public CommandResult<InfoEntity> getInfo() {
        DoGrpcWork<InfoEntity> work = () ->
        {
            InfoEntity entity = new InfoEntity();

            //also getinfo to find default address
            RpcGetInfoReply reply = this.getBlockingStub().getInfo(
                    RpcGetInfoRequest.newBuilder().build());

            if (reply.hasDefaultAddress()) {
                entity.setDefaultAddress(formatAddress(reply.getDefaultAddress().getAddress()));
            }
            //entity.setDefaultAmount(resultGetInfo.);
            entity.setLastBlockHash(formatHash(reply.getLastBlock().getHash()));
            entity.setLastBlockHeight(reply.getLastBlock().getNumber());

            entity.transactionFeePerByte = reply.getTransactionFee();
            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);
    }

    private static ByteString convertAddressToByteString(String addressString)
    {
        if (AddressUtility.isValidStandardAddress(addressString))
        {
            return ByteStringUtility.base58ToByteString(addressString);
        }
        else
        {
            return ByteStringUtility.base59ToByteString(addressString);
        }
    }

    public CommandResult<SendAmountEntity> sendAmount(SendInput sendInput) {
        DoGrpcWork<SendAmountEntity> work = () ->
        {
            SendAmountEntity entity = new SendAmountEntity();
            entity.input = sendInput;

            String fromAddress = sendInput.fromAddress;

            //RpcGetWalletTransactionsRequest.Builder requestBuilder = RpcGetWalletTransactionsRequest.newBuilder();
            RpcSendCoinsRequest.Builder builder = RpcSendCoinsRequest.newBuilder();
            builder.addAmounts(RpcOutput
                    .newBuilder()
                    .setAddress(convertAddressToByteString(sendInput.targetAddress))
                    .setAmount(sendInput.sentAmount)
            );

            if (fromAddress != null)
            {
                builder.setSourceAddress(convertAddressToByteString(fromAddress));
            }

            RpcSendCoinsReply reply = _blockingStub.sendCoins(builder.build());

            //get TX result
            for (ByteString bsTX : reply.getTxIdsList()) {
                String tx = Utility.bytesToHex(bsTX.toByteArray());
                entity.getOutputTxIdList().add(tx);
            }

            _logger.info("GRPC sendCoins: TxFeePerByte={}, Amount={}, TargetAddress={}, FromAddress='{}'",
                    VbkUtils.convertAtomicToVbkString(sendInput.txFeePerByte),
                    VbkUtils.convertAtomicToVbkString(sendInput.sentAmount),
                    sendInput.targetAddress,
                    sendInput.fromAddress);

            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, true);
    }

    public CommandResult<Void> setTxFeePerByte(long feePerByte) {
        DoGrpcWork<Void> work = () ->
        {
            RpcProtocolReply reply = this.getBlockingStub().setTransactionFee(
                    RpcSetTransactionFeeRequest.newBuilder()
                            .setAmount(feePerByte)
                            .build());

            _logger.info("GRPC setTransactionFee: {}", VbkUtils.convertAtomicToVbkString(feePerByte));

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);

    }

    public CommandResult<Void> backupWallet(String targetFolder) {
        DoGrpcWork<Void> work = () ->
        {
            RpcBackupWalletReply reply = this.getBlockingStub().backupWallet(
                    RpcBackupWalletRequest.newBuilder()
                            .setTargetLocation(ByteString.copyFrom(targetFolder.getBytes()))
                            .build());
            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);
    }

    public CommandResult<Void> importwallet(String backupFile) {

        //Cache the password, as unlocking the wallet clears it, and importWallet needs it twice: (1) unlockcommand, (2) param
        String cachePassword = _passphrase;

        DoGrpcWork<Void> work = () ->
        {
            //ImportWallet is special:
            String password = "";
            if (cachePassword != null)
                password = cachePassword;

            RpcImportWalletReply reply = this.getBlockingStub().importWallet(
                    RpcImportWalletRequest.newBuilder()
                            .setPassphrase(password)
                            .setSourceLocation(ByteString.copyFrom(backupFile.getBytes()))
                            .build());

            _logger.info("GRPC importWallet: {}", backupFile);

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, true);
    }

    public CommandResult<PendingTransactionEntity> drainAddress(String sourceAddress, String targetAddress) {
        DoGrpcWork<PendingTransactionEntity> work = () ->
        {
            PendingTransactionEntity entity = new PendingTransactionEntity();

            RpcDrainAddressReply reply = this.getBlockingStub().drainAddress(
                    RpcDrainAddressRequest.newBuilder()
                            .setSourceAddress(ByteStringUtility.base58ToByteString(sourceAddress))
                            .setDestinationAddress(ByteStringUtility.base58ToByteString(targetAddress))
                            .build());

            _logger.info("GRPC drainAddress: source:{}, target:{}", sourceAddress, targetAddress);

            RpcTransaction transaction = reply.getTransaction();

            entity.txId = Utility.bytesToHex(transaction.getTxId().toByteArray());
            entity.sizeBytes = transaction.getSize();
            entity.totalFeeAtomic = transaction.getTransactionFee();
            entity.sourceAmountAtomic = transaction.getSourceAmount();
            entity.setTransactionType(transaction.getType().name());

            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, true);
    }

    public CommandResult<Void> setDefaultAddress(String newAddress) {
        DoGrpcWork<Void> work = () ->
        {
            RpcSetDefaultAddressReply reply = this.getBlockingStub().setDefaultAddress(
                    RpcSetDefaultAddressRequest.newBuilder()
                            .setAddress(ByteStringUtility.base58ToByteString(newAddress))
                            .build());

            _logger.info("GRPC setDefaultAddress: {}", newAddress);

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);
    }

    public CommandResult<List<AddressBalanceEntity>> getAddressBalances() {
        DoGrpcWork<List<AddressBalanceEntity>> work = () ->
        {
            List<AddressBalanceEntity> entity = new ArrayList<>();

            RpcGetBalanceReply reply = this.getBlockingStub().getBalance(
                    RpcGetBalanceRequest.newBuilder().build());

            //Populate first so we can look it up:
            HashMap<String, Long> unconfirmed = new HashMap<>();
            for (RpcOutput o : reply.getUnconfirmedList()) {
                unconfirmed.put(formatAddress(o.getAddress()), o.getAmount());
            }

            for (RpcAddressBalance o : reply.getConfirmedList()) {
                AddressBalanceEntity address = new AddressBalanceEntity();
                address.setAddress(formatAddress(o.getAddress()));
                address.setAmountConfirmedAtomic(o.getUnlockedAmount()); //only show unlocked, not locked

                //merge in unconfirmed
                long d2 = unconfirmed.get(address.getAddress());
                address.setAmountPendingAtomic(d2);

                entity.add(address);
            }
            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);

    }

    public CommandResult<List<PendingTransactionEntity>> getPendingTransaction(List<String> lookupTxIds) {
        DoGrpcWork<List<PendingTransactionEntity>> work = () ->
        {
            List<PendingTransactionEntity> entities = new ArrayList<>();

            RpcGetPendingTransactionsReply reply = this.getBlockingStub().getPendingTransactions(
                        RpcGetPendingTransactionsRequest.newBuilder()
                                .build());
            //have list, apply filter
            for (RpcTransaction transaction : reply.getTransactionsList()) {
                String resultTxId = Utility.bytesToHex(transaction.getTxId().toByteArray());

                if (resultTxId != null && lookupTxIds.contains(resultTxId)) {
                    //found it!
                    PendingTransactionEntity entity = new PendingTransactionEntity();
                    entity.txId = resultTxId;
                    entity.sizeBytes = transaction.getSize();
                    entity.totalFeeAtomic = transaction.getTransactionFee();
                    entity.sourceAmountAtomic = transaction.getSourceAmount();
                    entity.setTransactionType(transaction.getType().name());

                    entities.add(entity);

                    if (entities.size() == lookupTxIds.size())
                    {
                        //shortcut --> found all the lookups
                        break;
                    }
                }
            }

            return new OutputContainer<>(entities, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, false);
    }

    public CommandResult<NewAddressEntity> getNewAddress() {
        DoGrpcWork<NewAddressEntity> work = () ->
        {
            NewAddressEntity entity = new NewAddressEntity();

            RpcGetNewAddressReply reply = this.getBlockingStub().getNewAddress(
                    RpcGetNewAddressRequest.newBuilder().build());

            entity.setNewAddress(Utility.bytesToBase58(reply.getAddress().toByteArray()));

            _logger.info("GRPC created new address: {}", entity.getNewAddress());

            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, true);

    }

    //Returns private key
    public CommandResult<String> dumpPrivateKey(String strAddress) {
        DoGrpcWork<String> work = () ->
        {
            String entity = new String();

            RpcDumpPrivateKeyReply reply = this.getBlockingStub().dumpPrivateKey(
                    RpcDumpPrivateKeyRequest.newBuilder()
                            .setAddress(ByteStringUtility.base58ToByteString(strAddress))
                            .build());

            entity = Utility.bytesToHex(reply.getPrivateKey().toByteArray());

            _logger.info("GRPC dumpPrivateKey address: {}", strAddress);

            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, true);
    }

    //Returns address
    public CommandResult<String> importPrivateKey(String privateKeyHexString) {
        DoGrpcWork<String> work = () ->
        {
            String entity = new String();

            RpcImportPrivateKeyReply reply = this.getBlockingStub().importPrivateKey(
                    RpcImportPrivateKeyRequest.newBuilder()
                            .setPrivateKey(ByteStringUtility.hexToByteString(privateKeyHexString))
                            .build());

            entity = Utility.bytesToBase58(reply.getResultantAddress().toByteArray());

            _logger.info("GRPC importPrivateKey address: {}", entity);

            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work, true);
    }

    public CommandResult<Void> refreshWalletCache() {
        DoGrpcWork<Void> work = () ->
        {
            RpcProtocolReply reply = this.getBlockingStub().refreshWalletCache(
                    RpcRefreshWalletCacheRequest.newBuilder().build());

            _logger.info("GRPC refreshWalletCache");

            return new OutputContainer<>(null, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);

    }

    //NOTE --> break convention and have this just return JSON as a one-off function?
    public String getDiagnosticInfoRaw() {

        RpcGetDiagnosticInfoReply reply = this.getBlockingStub().getDiagnosticInfo(
                RpcGetDiagnosticInfoRequest.newBuilder().build());

        if (reply.getSuccess()) {
            return (new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(reply));
        }
        else
        {
            return "";
        }
    }

    public CommandResult<WalletTransactionsEntity> getWalletTransactions(String myAddress,
        int pageNumber, int resultsPerPage, boolean isPending) {
        DoGrpcWork<WalletTransactionsEntity> work = () ->
        {
            RpcGetWalletTransactionsRequest.Builder requestBuilder = RpcGetWalletTransactionsRequest.newBuilder();
            WalletTransactionsEntity entity = new WalletTransactionsEntity();
            List<WalletTransactionEntity> items = new ArrayList<>();

            requestBuilder.setRequestType(RpcGetWalletTransactionsRequest.Type.QUERY);

            //Set input params:
            if (myAddress == null)
            {
                //returns all, ignoring any filter
                if (isPending) {
                    requestBuilder.setRequestType(RpcGetWalletTransactionsRequest.Type.QUERY);
                } else {
                    //LIST --> returns all, ignoring any filter
                    requestBuilder.setRequestType(RpcGetWalletTransactionsRequest.Type.LIST);
                }
            }
            else
            {
                requestBuilder.setAddress(ByteStringUtility.base58ToByteString(myAddress));
            }

            //Single query will not return both
            RpcTransactionMeta.Status filterStatus = RpcTransactionMeta.Status.UNKNOWN;
            if (isPending)
            {
                //Pending
                filterStatus = RpcTransactionMeta.Status.PENDING;
            }
            else
            {
                //Confirmed
                filterStatus = RpcTransactionMeta.Status.CONFIRMED;
            }


            requestBuilder.setTransactionType(RpcWalletTransaction.Type.NOT_SET);
            requestBuilder.setStatus(filterStatus);
            requestBuilder.setPage(RpcPaging.newBuilder()
                    .setPageNumber(pageNumber)
                    .setResultsPerPage(resultsPerPage).build());

            RpcGetWalletTransactionsReply reply = getBlockingStub()
                    .getWalletTransactions(requestBuilder.build());

            String cacheState = reply.getCacheState().name();
            entity.setCacheState(cacheState);

            //Get error message
            String replyMessage = reply.getMessage();
            if (replyMessage != null && replyMessage.length() == 0)
            {
                //nothing there... check for details
                if (reply.getResultsCount() > 0)
                {
                    replyMessage = "Address: " + myAddress + ", "
                            + reply.getResults(0).getMessage()
                            + ", " + reply.getResults(0).getDetails();
                }
            }
            entity.setReplyMessage(replyMessage);

            if (entity.getCacheState() != WalletTransactionsEntity.CacheState.CURRENT)
            {
                _logger.info("Wallet CacheState:{}, Address:{}, isPending:{}, Message: {}",
                        entity.getCacheState(), myAddress, isPending, replyMessage);
            }

            //loop through
            int iCount = reply.getTransactionsCount();
            _logger.info("Found {} wallet transactions for address={}, isPending={}", iCount, myAddress, isPending);
            for (int i = 0; i < iCount; i++) {
                RpcWalletTransaction t = reply.getTransactions(i);
                WalletTransactionEntity row = new WalletTransactionEntity();

                try {
                    //force an error
                    //row = null;

                    row.setTxId(Utility.bytesToHex(t.getTxId().toByteArray()));
                    row.setTimestamp(Utils.convertFromEpoch(t.getTimestamp()));

                    //  --> granular, as it has TX id
                    row.amount = t.getNetAmount();

                    row.setConfirmations(t.getMeta().getConfirmations());
                    //row.setIsCredit(t.getNetAmount() > 0);

                    //get block
                    byte[] header = t.getMeta().getBlockHeader().toByteArray();
                    if (header.length > 0) {
                        //blockHeader won't exist yet for pending
                        row.setBlockHeight(BlockUtility.extractBlockHeightFromBlockHeader(header));
                    }

                    String txType = t.getType().name();
                    row.setTxType(txType);

                    //STATUS:
                    //    UNKNOWN = 0;
                    //    PENDING = 1;
                    //    CONFIRMED = 2;
                    //    DEAD = 3;
                    row.setStatus(t.getMeta().getStatus().name());

                    //if RECEIVED, then get who receive from, if SENT, then get who sent TO
                    String s2 = Utility.bytesToBase58(t.getInput().getAddress().toByteArray());
                    row.setAddressMine(formatAddress(t.getAddress()));

                    if (txType.equals("RECEIVED")) {
                        row.setAddressFrom(formatAddress(t.getInput().getAddress()));
                        //don't crash, do safe check
                        if (t.getOutputsCount() > 0) {
                            row.setAddressTo(formatAddress(t.getOutputs(0).getAddress()));   //--> my address!
                        }
                    } else if (txType.equals("SENT")) {
                        //Could be sent to many addresses:
                        row.setAddressFrom(formatAddress(t.getInput().getAddress()));   //--> my address!
                        if (t.getOutputsCount() > 0) {
                            row.setAddressTo(formatAddress(t.getOutputs(0).getAddress()));   //--> could be many
                        }
                    }

                }
                catch (Exception ex2)
                {
                    _logger.error("Error parsing RpcWalletTransaction. Index={}, Transaction={}, Exception={}",
                            i, t.toString(), Utils.getExcetionToString(ex2));

                    if (formatAddress(t.getInput().getAddress()).length() > 40)
                    {
                        //GRPC mismatch issue. This should never happen
                        _logger.info("Developer error - bad GRPC, the following should be an address. If it looks like a TX then there is a mismatch with GRPC: {}", formatAddress(t.getInput().getAddress()));
                    }
                    throw ex2;
                }

                //override default behavior --> for pending, only add if address matches (default GRPC returns all for pending)
                if (isPending) {
                    if (myAddress == null)
                    {
                        //Pending, but get all --> then include
                        //that means get all, which is the default behavior for this GRPC command
                        items.add(row);
                    }
                    else if (myAddress.equals(row.getAddressMine()))
                    {
                        //Pending, but a specific address --> filter to just that address
                        items.add(row);
                    }
                }
                else {
                    //already filters
                    items.add(row);
                }
            }
            //NOTE --> cannot get RpcProcoloReply from reply, so pass in defaults
            entity.setWalletTransactions(items);
            return new OutputContainer<>(entity, true, null);
        };

        return templateCommand(work);
    }

    public CommandResult<List<PoPEndorsementInfoEntity>> getPoPEndorsementInfo(int searchLength) {
        DoGrpcWork<List<PoPEndorsementInfoEntity>> work = () ->
        {
            RpcGetPopEndorsementsInfoRequest request
                    = RpcGetPopEndorsementsInfoRequest.newBuilder()
                    .setSearchLength(searchLength)
                    .build();
            RpcGetPopEndorsementsInfoReply reply = this.getBlockingStub().getPopEndorsementsInfo(request);

            List<PoPEndorsementInfoEntity> entity = reply.getPopEndorsementsList()
                    .stream()
                    .map(PoPEndorsementInfoEntity::new)
                    .collect(Collectors.toList());

            return new OutputContainer<>(entity, reply.getSuccess(), reply.getResultsList());
        };
        return templateCommand(work);

    }
}

