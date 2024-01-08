package io.github.dankosik.investstarterdemojava;

import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllCandles;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllLastPrices;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllOrderBooks;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTrades;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleAllTradingStatuses;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleCandle;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleLastPrice;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleOrderBook;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTrade;
import io.github.dankosik.starter.invest.annotation.marketdata.HandleTradingStatus;
import io.github.dankosik.starter.invest.annotation.operation.HandleAllPortfolios;
import io.github.dankosik.starter.invest.annotation.operation.HandleAllPositions;
import io.github.dankosik.starter.invest.annotation.operation.HandlePortfolio;
import io.github.dankosik.starter.invest.annotation.operation.HandlePosition;
import io.github.dankosik.starter.invest.annotation.order.HandleAllOrders;
import io.github.dankosik.starter.invest.annotation.order.HandleOrder;
import io.github.dankosik.starter.invest.contract.marketdata.candle.AsyncCandleHandler;
import io.github.dankosik.starter.invest.contract.marketdata.lastprice.AsyncLastPriceHandler;
import io.github.dankosik.starter.invest.contract.marketdata.orderbook.AsyncOrderBookHandler;
import io.github.dankosik.starter.invest.contract.marketdata.status.AsyncTradingStatusHandler;
import io.github.dankosik.starter.invest.contract.marketdata.trade.AsyncTradeHandler;
import io.github.dankosik.starter.invest.contract.marketdata.trade.BlockingTradeHandler;
import io.github.dankosik.starter.invest.contract.operation.portfolio.AsyncPortfolioHandler;
import io.github.dankosik.starter.invest.contract.operation.positions.AsyncPositionHandler;
import io.github.dankosik.starter.invest.contract.orders.AsyncOrderHandler;
import io.github.dankosik.starter.invest.processor.marketdata.BlockingCandleStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.marketdata.BlockingLastPriceStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.marketdata.BlockingOrderBookStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.marketdata.BlockingTradeStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.marketdata.BlockingTradingStatusStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.marketdata.CandleStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.marketdata.LastPriceStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.marketdata.OrderBookStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.marketdata.TradeStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.marketdata.TradingStatusStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.marketdata.common.AsyncMarketDataStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.marketdata.common.MarketDataStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.operation.BlockingPortfolioStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.operation.BlockingPositionsStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.operation.PortfolioStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.operation.PositionsStreamProcessorAdapterFactory;
import io.github.dankosik.starter.invest.processor.order.BlockingOrdersStreamProcessorAdapter;
import io.github.dankosik.starter.invest.processor.order.OrdersStreamProcessorAdapterFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.piapi.contract.v1.Candle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.OrderBook;
import ru.tinkoff.piapi.contract.v1.OrderTrades;
import ru.tinkoff.piapi.contract.v1.PortfolioResponse;
import ru.tinkoff.piapi.contract.v1.PositionData;
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval;
import ru.tinkoff.piapi.contract.v1.Trade;
import ru.tinkoff.piapi.contract.v1.TradingStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class InvestStarterDemoJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestStarterDemoJavaApplication.class, args);
    }

}

/**
 * Обработка каждого трейда для выбранного тикера/figi/instrumentUid.
 * Использование instrumentType имеет смысл только если вы используете ticker, вместо figi или instrumentUid.
 * Если вы используете ticker при старте приложения будет выполнен запрос на поиск instrumentUid по переданному тикеру.
 * instrumentType нужен лишь для того чтобы сделать это за меньшее количество запросов к api, и с целью уменьшит трату лимитов
 * Блокирующие хендлеры рекомендуется юзать на jdk21+, исполнение будет на виртуальных потоках
 */
@HandleTrade(ticker = "SiH4")
class BlockingDollarHandler implements BlockingTradeHandler {

    @Override
    public void handleBlocking(@NotNull Trade trade) {
        System.out.println("BlockingDollarHandler: " + trade);
    }
}


/**
 * Хендлеров может быть сколько угодно, все они будут обрабатываться параллельно.
 * Если указанный тикер был хотя бы в одном из других хендлеров, то instrumentType можно не использовать.
 * Новые запросы для получения тикера не будут исполняться
 */
@HandleTrade(ticker = "SiH4")
class AsyncDollarHandler implements AsyncTradeHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull Trade trade) {
        return CompletableFuture.runAsync(() -> System.out.println("AsyncDollarHandler: " + trade));
    }
}

/**
 * обработка всех трейдов (опция beforeEachTradesHandler означает что выполнится этот handler перед всеми остальными)
 */
@HandleAllTrades(beforeEachTradesHandler = true)
class CommonBeforeEachTradesHandler implements AsyncTradeHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull Trade trade) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonBeforeEachTradesHandler: " + trade));
    }
}

/**
 * обработка всех трейдов для выбранных тикеров(опция afterEachTradesHandler означает что выполнится этот handler после всех остальных)
 */
@HandleAllTrades(
        tickers = {"CRH4", "BRG4", "SBER", "LKOH"},
        afterEachTradesHandler = true
)
class CommonAfterEachTradesHandler implements AsyncTradeHandler {
    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull Trade trade) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonAfterEachTradesHandler: " + trade));
    }
}

/**
 * обработка изменения последней цены для выбранного тикера/figi/instrumentUid
 */
@HandleLastPrice(ticker = "SiH4")
class DollarLastPriceHandler implements AsyncLastPriceHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull LastPrice lastPrice) {
        return CompletableFuture.runAsync(() -> System.out.println("DollarLastPriceHandler: " + lastPrice));
    }
}

/**
 * обработка изменения последней цены всех инструментов (опция beforeEachLastPriceHandler означает что выполнится этот handler перед всеми остальными)
 */
@HandleAllLastPrices(beforeEachLastPriceHandler = true)
class CommonBeforeEachLastPriceHandler implements AsyncLastPriceHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull LastPrice lastPrice) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonBeforeEachLastPriceHandler: " + lastPrice));
    }
}

/**
 * обработка изменения последней цены всех инструментов (опция afterEachLastPriceHandler означает что выполнится этот handler после всех остальных)
 */
@HandleAllLastPrices(afterEachLastPriceHandler = true)
class CommonAfterEachLastPriceHandler implements AsyncLastPriceHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull LastPrice lastPrice) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonAfterEachLastPriceHandler: " + lastPrice));
    }
}

/**
 * обработка изменений сткана для выбранного тикера/figi/instrumentUid
 */
@HandleOrderBook(ticker = "SiH4")
class DollarOrderBookHandler implements AsyncOrderBookHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull OrderBook orderBook) {
        return CompletableFuture.runAsync(() -> System.out.println("DollarOrderBookHandler: " + orderBook));
    }
}

/**
 * обработка изменения стакана всех инструментов (опция beforeEachOrderBookHandler означает что выполнится этот handler перед всеми остальными)
 */
@HandleAllOrderBooks(beforeEachOrderBookHandler = true)
class CommonBeforeEachOrderBookHandler implements AsyncOrderBookHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull OrderBook orderBook) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonBeforeEachOrderBookHandler: " + orderBook));
    }
}

/**
 * обработка изменения стакана всех инструментов (опция afterEachOrderBookHandler означает что выполнится этот handler после всех остальных)
 */
@HandleAllOrderBooks(afterEachOrderBookHandler = true)
class CommonAfterEachOrderBookHandler implements AsyncOrderBookHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull OrderBook orderBook) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonAfterEachOrderBookHandler: " + orderBook));
    }
}

/**
 * обработка свечи для выбранного тикера/figi/instrumentUid и выбранного интервала.
 * subscriptionInterval нужен чтобы выбрать интервал который будет обрабатывать этот хендлер
 */
@HandleCandle(
        ticker = "SiH4",
        subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class DollarCandleHandler implements AsyncCandleHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull Candle candle) {
        return CompletableFuture.runAsync(() -> System.out.println("DollarCandleHandler: " + candle));
    }
}

/**
 * обработка всех свеч выбранного интервала (опция beforeEachCandleHandler означает что выполнится этот handler перед всеми остальными)
 * subscriptionInterval нужен чтобы выбрать интервал который будет обрабатывать этот хендлер
 */
@HandleAllCandles(
        subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE,
        tickers = {"SiH4", "SBER"}
)
class CommonBeforeEachCandleHandler implements AsyncCandleHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull Candle candle) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonBeforeEachCandleHandler: " + candle));
    }
}

/**
 * обработка всех свеч выбранного интервала (опция afterEachCandleHandler означает что выполнится этот handler после всех остальных)
 * subscriptionInterval нужен чтобы выбрать интервал который будет обрабатывать этот хендлер
 */
@HandleAllCandles(
        afterEachCandleHandler = true,
        subscriptionInterval = SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_MINUTE
)
class CommonAfterEachCandleHandler implements AsyncCandleHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull Candle candle) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonAfterEachCandleHandler: " + candle));
    }
}

/**
 * обработка изменений торгового статуса для выбранного тикера/figi/instrumentUid
 */
@HandleTradingStatus(ticker = "SiH4")
class DollarTradingStatusHandler implements AsyncTradingStatusHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull TradingStatus tradingStatus) {
        return CompletableFuture.runAsync(() -> System.out.println("DollarTradingStatusHandler: " + tradingStatus));
    }
}

/**
 * обработка изменений торгового статуса для всех инструментов (опция beforeEachTradingStatusHandler означает что выполнится этот handler перед всеми остальными
 */
@HandleAllTradingStatuses(beforeEachTradingStatusHandler = true)
class CommonBeforeEachTradingStatusHandler implements AsyncTradingStatusHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull TradingStatus tradingStatus) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonBeforeEachTradingStatusHandler: " + tradingStatus));
    }
}

/**
 * обработка изменений торгового статуса для всех инструментов (опция beforeEachTradingStatusHandler означает что выполнится этот handler после всех остальных
 */
@HandleAllTradingStatuses(afterEachTradingStatusHandler = true)
class CommonAfterEachTradingStatusHandler implements AsyncTradingStatusHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull TradingStatus tradingStatus) {
        return CompletableFuture.runAsync(() -> System.out.println("CommonAfterEachTradingStatusHandler: " + tradingStatus));
    }
}

/**
 * обработка изменения позиций портфеля для конкретного аккаунта
 */
@HandlePortfolio(account = "accountId") //замените на ваш актуальный
class PortfolioHandler implements AsyncPortfolioHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull PortfolioResponse portfolioResponse) {
        return CompletableFuture.runAsync(() -> System.out.println("PortfolioHandler: " + portfolioResponse));
    }
}

/**
 * обработка изменения позиций портфеля для нескольких аккаунтов
 */
@HandleAllPortfolios(accounts = {"accountId", "accountId2"}) //замените на ваши актуальные
class AllPortfolioHandler implements AsyncPortfolioHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull PortfolioResponse portfolioResponse) {
        return CompletableFuture.runAsync(() -> System.out.println("AllPortfolioHandler: " + portfolioResponse));
    }
}

/**
 * обработка изменения позиций для конкретного аккаунта
 */
@HandlePosition(account = "accountId") //замените на ваш актуальный
class PositionHandler implements AsyncPositionHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull PositionData positionData) {
        return CompletableFuture.runAsync(() -> System.out.println("PositionHandler: " + positionData));
    }
}

/**
 * обработка изменения позиций для нескольких аккаунтов
 */
@HandleAllPositions(accounts = {"accountId", "accountId2"}) //замените на ваши актуальные
class AllPositionHandler implements AsyncPositionHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull PositionData positionData) {
        return CompletableFuture.runAsync(() -> System.out.println("AllPositionHandler: " + positionData));
    }
}

/**
 * обработка ордеров для конкретного аккаунта и конкретного тикера
 */
@HandleOrder(account = "accountId", ticker = "SiH4")
class OrderHandler implements AsyncOrderHandler {

    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull OrderTrades orderTrades) {
        return CompletableFuture.runAsync(() -> System.out.println("OrderHandler: " + orderTrades));
    }
}

/**
 * обработка всех ордеров из нескольких аккаунтов
 */
@HandleAllOrders(accounts = {"accountId", "accountId2"}) //замените на ваши актуальные
class AllOrderHandler implements AsyncOrderHandler {
    @NotNull
    @Override
    public CompletableFuture<Void> handleAsync(@NotNull OrderTrades orderTrades) {
        return CompletableFuture.runAsync(() -> System.out.println("AllOrderHandler: " + orderTrades));
    }
}

@Configuration
class Config {

    /**
     * Можно обрабатывать все события marketData
     */
    @Bean
    public AsyncMarketDataStreamProcessorAdapter marketDataStreamProcessorAdapter() {
        return MarketDataStreamProcessorAdapterFactory
                .withTickers(List.of("CRH4", "BRG4", "SBER", "LKOH"))
                .createAsyncHandler(marketDataResponse ->
                        CompletableFuture.runAsync(() -> System.out.println("marketDataStreamProcessorAdapter:" + marketDataResponse))
                );
        //        .withFigies(listOf("BBG004730N88")) можно использовать вместо withTickers
//        .withInstrumentUids(listOf("e6123145-9665-43e0-8413-cd61b8aa9b13")) можно использовать вместо withTickers
//        .runAfterEachTradeHandler(true)  опционально
//        .runBeforeEachCandleHandler(true) опционально
//        .runAfterEachLastPriceHandler(true) опционально
//        .runBeforeEachOrderBookHandler(true) опционально
    }

    /**
     * Аналог HandleAllLastPrice
     */
    @Bean
    public BlockingLastPriceStreamProcessorAdapter coroutineLastPriceStreamProcessorAdapter() {
        return LastPriceStreamProcessorAdapterFactory
//            .runAfterEachLastPriceHandler(true) опционально
//            .runBeforeEachLastPriceHandler(true) опционально
                .withTickers(List.of("CRH4", "BRG4", "SBER", "LKOH"))
                .createBlockingHandler(lastPrice -> System.out.println("LastPriceStreamProcessorAdapterFactory" + lastPrice)); // для jdk 21+ BlockingHandler будет исполнен в виртуальном потоке
    }

    /**
     * Аналог HandleAllTrades
     */
    @Bean
    public BlockingTradeStreamProcessorAdapter coroutineTradeStreamProcessorAdapter() {
        return TradeStreamProcessorAdapterFactory
                .withTickers(List.of("CRH4", "BRG4", "SBER", "LKOH"))
                .createBlockingHandler(trade -> System.out.println("TradeStreamProcessorAdapterFactory" + trade)); // для jdk 21+ BlockingHandler будет исполнен в виртуальном потоке
    }

    /**
     * Аналог HandleAllTradingStatuses
     */
    @Bean
    public BlockingTradingStatusStreamProcessorAdapter coroutineTradingStatusStreamProcessorAdapter() {
        return TradingStatusStreamProcessorAdapterFactory
                .withTickers(List.of("CRH4", "BRG4", "SBER", "LKOH"))
                .createBlockingHandler(tradingStatus -> System.out.println("TradingStatusStreamProcessorAdapterFactory: " + tradingStatus));
    }

    /**
     * Аналог HandleAllCandles
     */
    @Bean
    public BlockingCandleStreamProcessorAdapter coroutineCandleStreamProcessorAdapter() {
        return CandleStreamProcessorAdapterFactory
                .withSubscriptionInterval(SubscriptionInterval.SUBSCRIPTION_INTERVAL_2_MIN)
                .waitClose(true)
                .withTickers(List.of("CRH4", "BRG4", "SBER", "LKOH"))
                .createBlockingHandler(candle -> System.out.println("BlockingCandleStreamProcessorAdapter" + candle));
    }

    /**
     * Аналог HandleAllOrderBooks
     */
    @Bean
    public BlockingOrderBookStreamProcessorAdapter coroutineOrderBookStreamProcessorAdapter() {
        return OrderBookStreamProcessorAdapterFactory
                .withTickers(List.of("CRH4", "BRG4", "SBER", "LKOH"))
                .createBlockingHandler(orderBook -> System.out.println("OrderBookStreamProcessorAdapterFactory" + orderBook));

    }

    /**
     * Аналог HandleAllPortfolios
     */
    @Bean
    public BlockingPortfolioStreamProcessorAdapter portfolioStreamProcessorAdapterFactory() {
        return PortfolioStreamProcessorAdapterFactory.withAccounts(List.of("accountId", "accountId2")) //замените на ваши актуальные
                .createBlockingHandler(portfolioStreamResponse -> System.out.println("BlockingPortfolioStreamProcessorAdapter" + portfolioStreamResponse));

    }

    /**
     * Аналог HandleAllPositions
     */
    @Bean
    public BlockingPositionsStreamProcessorAdapter positionsStreamProcessorAdapterFactory() {
        return PositionsStreamProcessorAdapterFactory.withAccounts(List.of("accountId", "accountId2")) //замените на ваши актуальные
                .createBlockingHandler(portfolioStreamResponse -> System.out.println("BlockingPositionsStreamProcessorAdapter" + portfolioStreamResponse));

    }

    /**
     * Аналог HandleAllPositions
     */
    @Bean
    public BlockingOrdersStreamProcessorAdapter ordersStreamProcessorAdapterFactory() {
        return OrdersStreamProcessorAdapterFactory
                .withTickers(List.of("SBER"))
                .withAccounts(List.of("accountId", "accountId2")) //замените на ваши актуальные
                .createBlockingHandler(portfolioStreamResponse -> System.out.println("BlockingOrdersStreamProcessorAdapterFactory" + portfolioStreamResponse));

    }
}