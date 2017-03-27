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

package io.bisq.core.trade.protocol.tasks.taker;

import io.bisq.common.taskrunner.TaskRunner;
import io.bisq.core.trade.Trade;
import io.bisq.core.trade.protocol.tasks.TradeTask;
import io.bisq.protobuffer.payload.crypto.PubKeyRingPayload;
import io.bisq.protobuffer.payload.trade.statistics.TradeStatistics;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TakerPublishTradeStatistics extends TradeTask {
    @SuppressWarnings({"WeakerAccess", "unused"})
    public TakerPublishTradeStatistics(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            // Taker only publishes if the maker uses an old version
            processModel.getP2PService().getNetworkNode().getConfirmedConnections()
                    .stream()
                    .filter(c -> c.getPeersNodeAddressOptional().isPresent() && c.getPeersNodeAddressOptional().get().equals(trade.getTradingPeerNodeAddress()))
                    .findAny()
                    .ifPresent(c -> {
                        TradeStatistics tradeStatistics = new TradeStatistics(trade.getOffer().getOfferPayload(),
                                trade.getTradePrice(),
                                trade.getTradeAmount(),
                                trade.getDate(),
                                (trade.getDepositTx() != null ? trade.getDepositTx().getHashAsString() : ""),
                                new PubKeyRingPayload(processModel.getPubKeyRing()));

                        final List<Integer> requiredCapabilities = tradeStatistics.getRequiredCapabilities();
                        final List<Integer> supportedCapabilities = c.getSupportedCapabilities();
                        boolean matches = false;
                        if (supportedCapabilities != null) {
                            for (int messageCapability : requiredCapabilities) {
                                for (int connectionCapability : supportedCapabilities) {
                                    if (messageCapability == connectionCapability) {
                                        matches = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!matches) {
                            log.debug("We publish tradeStatistics because the maker uses an old version so we publish to have at least 1 data item published.");
                            processModel.getP2PService().addData(tradeStatistics, true);
                        } else {
                            log.trace("We do not publish tradeStatistics because the maker support the capabilities.");
                        }
                    });
            complete();
        } catch (Throwable t) {
            failed(t);
        }
    }
}