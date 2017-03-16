/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.network_messages.trade.protocol.trade.messages;

import com.google.protobuf.ByteString;
import io.bisq.app.Version;
import io.bisq.common.util.Utilities;
import io.bisq.common.wire.proto.Messages;
import io.bisq.network_messages.btc.data.RawTransactionInput;
import io.bisq.network_messages.payment.payload.PaymentAccountContractData;
import io.bisq.network_messages.protocol.trade.TradeMessage;
import io.bisq.network_messages.util.ProtoBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Immutable

// TODO check if it should not implement MailboxMessage as well?
public final class PublishDepositTxRequest extends TradeMessage {
    // That object is sent over the wire, so we need to take care of version compatibility.
    private static final long serialVersionUID = Version.P2P_NETWORK_VERSION;

    private static final Logger log = LoggerFactory.getLogger(PublishDepositTxRequest.class);

    public final PaymentAccountContractData offererPaymentAccountContractData;
    public final String offererAccountId;
    public final String offererContractAsJson;
    public final String offererContractSignature;
    public final String offererPayoutAddressString;
    public final byte[] preparedDepositTx;
    public final List<RawTransactionInput> offererInputs;
    public final byte[] offererMultiSigPubKey;

    public PublishDepositTxRequest(String tradeId,
                                   PaymentAccountContractData offererPaymentAccountContractData,
                                   String offererAccountId,
                                   byte[] offererMultiSigPubKey,
                                   String offererContractAsJson,
                                   String offererContractSignature,
                                   String offererPayoutAddressString,
                                   byte[] preparedDepositTx,
                                   List<RawTransactionInput> offererInputs) {
        super(tradeId);
        this.offererPaymentAccountContractData = offererPaymentAccountContractData;
        this.offererAccountId = offererAccountId;
        this.offererMultiSigPubKey = offererMultiSigPubKey;
        this.offererContractAsJson = offererContractAsJson;
        this.offererContractSignature = offererContractSignature;
        this.offererPayoutAddressString = offererPayoutAddressString;
        this.preparedDepositTx = preparedDepositTx;
        this.offererInputs = offererInputs;

        log.trace("offererPaymentAccount size " + Utilities.serialize(offererPaymentAccountContractData).length);
        log.trace("offererTradeWalletPubKey size " + offererMultiSigPubKey.length);
        log.trace("preparedDepositTx size " + preparedDepositTx.length);
        log.trace("offererInputs size " + Utilities.serialize(new ArrayList<>(offererInputs)).length);
    }

    @Override
    public Messages.Envelope toProtoBuf() {
        Messages.Envelope.Builder baseEnvelope = ProtoBufferUtils.getBaseEnvelope();
        return baseEnvelope.setPublishDepositTxRequest(baseEnvelope.getPublishDepositTxRequestBuilder()
                .setMessageVersion(getMessageVersion())
                .setTradeId(tradeId)
                .setOffererPaymentAccountContractData((Messages.PaymentAccountContractData) offererPaymentAccountContractData.toProtoBuf())
                .setOffererAccountId(offererAccountId)
                .setOffererMultiSigPubKey(ByteString.copyFrom(offererMultiSigPubKey))
                .setOffererContractAsJson(offererContractAsJson)
                .setOffererContractSignature(offererContractSignature)
                .setOffererPayoutAddressString(offererPayoutAddressString)
                .setPreparedDepositTx(ByteString.copyFrom(preparedDepositTx))
                .addAllOffererInputs(offererInputs.stream().map(rawTransactionInput -> rawTransactionInput.toProtoBuf()).collect(Collectors.toList()))).build();
    }
}
